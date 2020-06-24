package ch.ethz.biol.cell.sgmn.objmask;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SgmnFailedException;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmnOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.plugin.image.bean.sgmn.watershed.minima.MinimaImposition;


// Imposes minima only in seed locations on the input channel, and performs the segmentation
public class ObjMaskSgmnMinimaImposition extends ObjMaskSgmnOne {

	// START BEAN PROPERTIES
	@BeanField
	private MinimaImposition minimaImposition;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection sgmn(
		Channel chnl,
		Optional<ObjectMask> mask,
		Optional<SeedCollection> seeds,
		ObjMaskSgmn sgmn
	) throws SgmnFailedException {

		if (!seeds.isPresent()) {
			throw new SgmnFailedException("seeds must be present");
		}
				
		try {
			Channel chnlWithImposedMinima = chnlWithImposedMinima(chnl, seeds.get(), mask );
			
			ObjectCollection omc = sgmn.sgmn(chnlWithImposedMinima, mask, seeds );
			
			return omc;
			
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	private Channel chnlWithImposedMinima(Channel chnl, SeedCollection seeds, Optional<ObjectMask> objMask) throws OperationFailedException {
		if (seeds.size()>0) {
			return minimaImposition.imposeMinima(chnl, seeds, objMask);
		} else {
			return chnl;
		}
	}

	public MinimaImposition getMinimaImposition() {
		return minimaImposition;
	}

	public void setMinimaImposition(MinimaImposition minimaImposition) {
		this.minimaImposition = minimaImposition;
	}
}
