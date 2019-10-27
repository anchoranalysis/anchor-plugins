package ch.ethz.biol.cell.imageprocessing.stack.provider;

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
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterMulti;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderMax;

class MaxChnls {

	public static Chnl apply( Chnl chnl1, Chnl chnl2, VoxelDataType outputType ) throws CreateException {
		Chnl max = maxChnls(chnl1, chnl2);
		return convert(max, outputType );
	}
	
	private static Chnl maxChnls( Chnl chnl1, Chnl chnl2 ) throws CreateException {
		if (chnl2!=null) {
			return ChnlProviderMax.createMax(chnl1, chnl2);
		} else {
			return chnl1;
		}
	}
	
	
	private static Chnl convert( Chnl chnl, VoxelDataType outputType ) {
		ChnlConverterMulti chnlConverter = new ChnlConverterMulti();
		return chnlConverter.convert(chnl, outputType);
	}
}
