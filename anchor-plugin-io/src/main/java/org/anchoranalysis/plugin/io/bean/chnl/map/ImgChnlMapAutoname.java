package org.anchoranalysis.plugin.io.bean.chnl.map;

/*
 * #%L
 * anchor-io
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

import java.util.List;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

/**
 * Names of the channels from the metadata if it exists, otherwise names in
 * pattern chnl-%d where %d is the index
 * 
 * @author owen
 *
 */
public class ImgChnlMapAutoname extends ImgChnlMapCreator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImgChnlMapAutoname() {
		super();
	}

	@Override
	public ImgChnlMap createMap(OpenedRaster openedRaster) throws CreateException {

		ImgChnlMap map = new ImgChnlMap();

		// null indicates that there are no names
		List<String> names = openedRaster.channelNames();

		try {
			for (int c = 0; c < openedRaster.numChnl(); c++) {

				String chnlName;
				if (names != null) {
					chnlName = names.get(c);
				} else {
					chnlName = String.format("chnl-%d", c);
				}
				map.add(new ImgChnlMapEntry(chnlName, c));
			}

		} catch (RasterIOException e) {
			throw new CreateException(e);
		}

		return map;
	}
}