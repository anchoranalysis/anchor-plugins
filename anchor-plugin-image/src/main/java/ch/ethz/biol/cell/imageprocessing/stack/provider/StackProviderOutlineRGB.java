package ch.ethz.biol.cell.imageprocessing.stack.provider;



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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderHolder;
import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderOutline;

public class StackProviderOutlineRGB extends StackProviderWithBackground {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider mask;
	
	@BeanField @OptionalBean
	private ChnlProvider chnlBlue;
	
	@BeanField
	private boolean mip = false;
	
	@BeanField
	private boolean force2D = false;
	
	@BeanField
	private boolean createShort = false;
	// END BEAN PROPERTIES
		
	@Override
	public Stack create() throws CreateException {
		
		BinaryChnl maskChnl = mask.create();
				
		try {
			boolean do3D = mip!=true || maskChnl.getDimensions().getZ()==1;
			
			return CalcOutlineRGB.apply(
				calcOutline(maskChnl),
				backgroundStack(do3D),
				createBlue(do3D, maskChnl.getDimensions()),
				createShort
			);
			
		} catch (InitException | OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	private Channel createBlue(boolean do3D, ImageDimensions dim) throws CreateException {
		Channel chnlBlue = createBlueMaybeProvider(dim);
		if (do3D) {
			return chnlBlue;
		} else {
			return chnlBlue.maxIntensityProjection();
		}
	}
	
	private Channel createBlueMaybeProvider(ImageDimensions dim) throws CreateException {
		if (chnlBlue!=null) {
			return chnlBlue.create();
		} else {
			return ChannelFactory.instance().createEmptyInitialised(dim, VoxelDataTypeUnsignedByte.instance);
		}
	}
	
	private BinaryChnl calcOutline( BinaryChnl mask ) throws OperationFailedException {
		BinaryChnl maskIn = mip ? mask.maxIntensityProj() : mask.duplicate(); 
		
		// We calculate outline of mask
		return createOutline(maskIn);
	}
	
	private BinaryChnl createOutline( BinaryChnl maskIn ) throws OperationFailedException {
		// We calculate outline of mask
		BinaryChnlProviderOutline cpOutline = new BinaryChnlProviderOutline();
		cpOutline.setForce2D(force2D);
		cpOutline.setBinaryChnl( new BinaryChnlProviderHolder(maskIn) );
		try {
			cpOutline.initRecursive( getSharedObjects(), getLogger() );
			return cpOutline.create();
		} catch (InitException | CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}

	public boolean isForce2D() {
		return force2D;
	}

	public void setForce2D(boolean force2d) {
		force2D = force2d;
	}

	public boolean isCreateShort() {
		return createShort;
	}

	public void setCreateShort(boolean createShort) {
		this.createShort = createShort;
	}

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}

	public ChnlProvider getChnlBlue() {
		return chnlBlue;
	}

	public void setChnlBlue(ChnlProvider chnlBlue) {
		this.chnlBlue = chnlBlue;
	}
}
