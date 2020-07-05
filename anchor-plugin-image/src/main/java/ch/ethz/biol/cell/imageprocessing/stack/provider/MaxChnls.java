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
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterMulti;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderMax;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class MaxChnls {

	public static Channel apply( Channel chnl1, Channel chnl2, VoxelDataType outputType ) throws CreateException {
		Channel max = maxChnls(chnl1, chnl2);
		return convert(max, outputType );
	}
	
	private static Channel maxChnls( Channel chnl1, Channel chnl2 ) throws CreateException {
		if (chnl2!=null) {
			return ChnlProviderMax.createMax(chnl1, chnl2);
		} else {
			return chnl1;
		}
	}
	
	
	private static Channel convert( Channel chnl, VoxelDataType outputType ) {
		ChannelConverterMulti chnlConverter = new ChannelConverterMulti();
		return chnlConverter.convert(chnl, outputType);
	}
}
