package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.nio.FloatBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToShort;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeShort;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.NativeImg;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;


// 
/**
 * 3x3 Sobel Filter
 * 
 * Out-of-bounds strategy = mirror
 * 
 * @author Owen Feehan
 *
 */
public class ChnlProviderGradientFilter extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3978226156945187112L;
	
	// START BEAN
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private double scaleFactor = 1.0;
	
	@BeanField
	private boolean outputShort=false;	// If true, outputs a short. Otherwise a byte
	// END BEAN

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}
	
	// From netlib
	// https://github.com/imglib/imglib2-algorithm-gpl/blob/master/src/main/java/net/imglib2/algorithm/pde/Gradient.java
	
	/**
	 * 
	 * @param input
	 * @param output
	 * @param doDimension
	 * @param scaleFactor
	 * @param centralDiff calculate central-difference instead of backward-difference (finite differences)
	 * @param norm do we apply a l2norm to our difference (useful for getting magnittude if working with more than 1 dimension)
	 * @param addSum adds a contstant after scale-factor has been applied (useful for shifting negative numbers into a positive range)
	 */
	static <T extends RealType<T>> void process( NativeImg<T,?> input, NativeImg<FloatType,FloatArray> output, boolean[] doDimension, float scaleFactor, boolean centralDiff, boolean norm, int addSum ) {
		
		Cursor< T > in = Views.iterable( input ).localizingCursor();
		RandomAccess< FloatType > oc = output.randomAccess();
		
		OutOfBounds< T > ra = Views.extendMirrorDouble( input ).randomAccess(); 

		
 		while ( in.hasNext() )
		{
			in.fwd();

			// Position neighborhood cursor;
			ra.setPosition( in );

			// Position output cursor
			for ( int i = 0; i < input.numDimensions(); i++ )
			{
				oc.setPosition( in.getLongPosition( i ), i );
			}

			// Central value
			float central = in.get().getRealFloat();

			double diffSum = 0.0;
			
			// Gradient
			for ( int i = 0; i < input.numDimensions(); i++ )
			{
				if ( !doDimension[ i ] )
				{
					continue;
				}
				ra.fwd( i );
				float diff = central - ra.get().getRealFloat();
				ra.bck( i );
				
				if (centralDiff) {
					ra.bck(i);
					diff += ra.get().getRealFloat();
					ra.fwd(i);
				}
				
				
				if (norm) {
					diffSum += Math.pow(diff,2.0);
				} else {
					diffSum += diff;
				}
			}
			
			float diffOut = (float) (norm ? Math.sqrt( diffSum ) : diffSum);
			
			oc.get().set( (diffOut*scaleFactor)+addSum );
		}

	}
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnlIn = chnlProvider.create();
		
		Chnl chnlIntermediate = ChnlFactory.instance().createEmptyInitialised( chnlIn.getDimensions(), VoxelDataTypeFloat.instance );
		VoxelBox<FloatBuffer> vb = chnlIntermediate.getVoxelBox().asFloat();

		boolean do3D = false;
		boolean[] doDimension = new boolean[] { true, true, do3D };
		
		NativeImg<FloatType,FloatArray> natOut = ImgLib2Wrap.wrapFloat(vb, true);
		
		if (chnlIn.getVoxelDataType().equals(VoxelDataTypeByte.instance)) {
			NativeImg<UnsignedByteType,ByteArray> natIn = ImgLib2Wrap.wrapByte(chnlIn.getVoxelBox().asByte(), true);
			process(natIn,natOut, doDimension, (float) scaleFactor, false, true, 0 );
		} else if (chnlIn.getVoxelDataType().equals(VoxelDataTypeShort.instance)) {
			NativeImg<UnsignedShortType,ShortArray> natIn = ImgLib2Wrap.wrapShort(chnlIn.getVoxelBox().asShort(), true );
			process(natIn,natOut, doDimension, (float) scaleFactor, false, true, 0);
		} else {
			throw new CreateException("Input type must be unsigned byte or short");
		}
		
		// convert to our output from the float
		ChnlConverter<?> converter = outputShort ? new ChnlConverterToShort() : new ChnlConverterToByte();
		return converter.convert(chnlIntermediate, ConversionPolicy.CHANGE_EXISTING_CHANNEL );
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public boolean isOutputShort() {
		return outputShort;
	}

	public void setOutputShort(boolean outputShort) {
		this.outputShort = outputShort;
	}

}
