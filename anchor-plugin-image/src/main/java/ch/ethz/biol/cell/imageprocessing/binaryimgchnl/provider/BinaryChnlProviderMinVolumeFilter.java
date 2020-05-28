package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolume;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.objmask.ops.BinaryChnlFromObjs;
import org.anchoranalysis.image.unitvalue.UnitValueException;

public class BinaryChnlProviderMinVolumeFilter extends BinaryChnlProviderOne {

	// START BEAN FIELDS
	@BeanField
	private UnitValueVolume minVolume = null;
	
	@BeanField
	private boolean inverted = false;
	// END BEAN FIELDS
	
	@Override
	public BinaryChnl createFromChnl( BinaryChnl chnl ) throws CreateException {
		return createMaskedImage(chnl);
	}

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	public UnitValueVolume getMinVolume() {
		return minVolume;
	}

	public void setMinVolume(UnitValueVolume minVolume) {
		this.minVolume = minVolume;
	}

	private BinaryChnl createMaskedImage( BinaryChnl bi ) throws CreateException {

		ObjMaskCollection objs = connectedComponents(bi, inverted);
		return BinaryChnlFromObjs.createFromObjs(objs, bi.getDimensions(), bi.getBinaryValues() );
	}
	
	private ObjMaskCollection connectedComponents( BinaryChnl bi, boolean inverted ) throws CreateException {
		
		int rslvMinNum;
		try {
			rslvMinNum = (int) Math.floor(
				minVolume.rslv(
					Optional.of(bi.getDimensions().getRes())
				)
			);
		} catch (UnitValueException e) {
			throw new CreateException(e);
		}

		CreateFromConnectedComponentsFactory createObjMasks = new CreateFromConnectedComponentsFactory(rslvMinNum);
		if (inverted) {
			
			BinaryValues bvInverted = bi.getBinaryValues().createInverted();
			BinaryChnl biInverted = new BinaryChnl( bi.getChnl(), bvInverted );	// In case we've inverted the binary valies

			return createObjMasks.createConnectedComponents(biInverted);
						
		} else {
			return createObjMasks.createConnectedComponents(bi);
		}
	}


}