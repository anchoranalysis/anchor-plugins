package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;

class AssignUtilities {
	
	public static void checkDims(Chnl chnl, BinaryChnl mask) throws CreateException {
		if (!chnl.getDimensions().equals(mask.getDimensions())) {
			throw new CreateException( String.format("chnl (%s) and mask (%s) must have the same dimensions", chnl.getDimensions().toString(), mask.getDimensions().toString() ) );
		}
	}
	
	public static void checkDims(Chnl chnl, Chnl chnlAssignFrom) throws CreateException {
		if (!chnl.getDimensions().equals(chnlAssignFrom.getDimensions())) {
			throw new CreateException( String.format("chnlSrc (%s) and chnlAssignFrom (%s) must have the same dimensions", chnl.getDimensions().toString(), chnlAssignFrom.getDimensions().toString() ) );
		}
	}
}
