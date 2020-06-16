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


import java.nio.ByteBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResult;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollection;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollectionFactory;

public class ChnlProviderConnectedComponentScore extends ChnlProviderOneObjsSource {
	
	// START BEAN PROPERTIES
	@BeanField
	private CalculateLevel calculateLevel;
	
	@BeanField
	private int intensityTolerance= 0;
	// END BEAN PROPERTIES
	
	@Override
	protected Channel createFromChnl(Channel chnl, ObjectCollection objsSource) throws CreateException {
	
		LevelResultCollection lrc = LevelResultCollectionFactory.createCollection(
			chnl,
			objsSource,
			calculateLevel,
			0,
			getLogger().getLogReporter()
		);
		
		Channel chnlOut = ChannelFactory.instance().createEmptyInitialised( chnl.getDimensions(), VoxelDataTypeUnsignedByte.instance );
		
		populateChnl( chnl, chnlOut, lrc );

		return chnlOut;
	}

	private int calcOutValue( int pixelVal, int level ) {
		
		if (pixelVal >= level ) {
			
			int diff = pixelVal - level;
			if (diff>intensityTolerance) {
				return 255;
			} else {
				return 128 + divideMultiply(diff,intensityTolerance, 127);
			}
		} else {
			
			int diff = level - pixelVal;
			if (diff>intensityTolerance) {
				return 0;
			} else {
				return 128 - divideMultiply(diff,intensityTolerance, 128);
			}			
		}
	}
	
	private static int divideMultiply( int numerator, int denominator, int mult ) {
		double div = ((double) numerator) / denominator;
		return (int) (div*mult);
	}
	
	private void populateChnl( Channel regionIn, Channel regionOut, LevelResultCollection lrc ) {
		
		VoxelBox<ByteBuffer> vbIn = regionIn.getVoxelBox().asByte();
		VoxelBox<ByteBuffer> vbOut = regionOut.getVoxelBox().asByte();
		
		assert( vbIn.extent().equals(vbOut.extent()) );
		
		Extent e = vbIn.extent();
		
		Point3d pnt = new Point3d();
		
		for( int z=0; z<e.getZ(); z++) {
			pnt.setZ(z);
			
			ByteBuffer bbIn = vbIn.getPixelsForPlane(z).buffer();
			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			
			int index = 0;
			for( int y=0; y<e.getY(); y++) {
				pnt.setY( y );
				
				for( int x=0; x<e.getX(); x++) {
					pnt.setX( x );
					
					int val = ByteConverter.unsignedByteToInt( bbIn.get(index) );
					
					// Find closest point
					LevelResult lr = lrc.findClosestResult(pnt);
					
					int out = calcOutValue(val, lr.getLevel() );
					
					bbOut.put( (byte) out );
					
					index++;
				}
			}
		}
	}
	
	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}

	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}

	public int getIntensityTolerance() {
		return intensityTolerance;
	}

	public void setIntensityTolerance(int intensityTolerance) {
		this.intensityTolerance = intensityTolerance;
	}
}
