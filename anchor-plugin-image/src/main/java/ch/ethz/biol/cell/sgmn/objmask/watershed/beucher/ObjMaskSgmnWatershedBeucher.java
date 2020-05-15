package ch.ethz.biol.cell.sgmn.objmask.watershed.beucher;

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
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;

import ch.ethz.biol.cell.sgmn.objmask.ObjMaskSgmnFloodFillStack;


// A 'rainfall' watershed algorithm
//
// See:
//  * The watershed transform in ITK - discussion and new developments
//		Beare and Lehmann, 2006  (Insight journal)
//
//
//  Uses seeds
//  Does not record a watershed lne
//  
public class ObjMaskSgmnWatershedBeucher extends ObjMaskSgmn {

	// START PROPERTIES
	@BeanField
	private ObjMaskSgmn sgmnFloodFill = new ObjMaskSgmnFloodFillStack();
	// END PROPERTIES

	@Override
	public ObjMaskCollection sgmn(Chnl chnl, SeedCollection seeds)
			throws SgmnFailedException {
		
		if (seeds==null || seeds.size()==0) {
			throw new SgmnFailedException("seeds are required for this algorithm");
		}
		throw new SgmnFailedException("unsupported sgmn");
	}
	
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			SeedCollection seeds) throws SgmnFailedException {

		return sgmn(chnl, seeds);
	}

}
