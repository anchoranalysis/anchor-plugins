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
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

/**
 * Names of the channels from the metadata if it exists, or after RGB, or by index
 * 
 * <p>Naming rules - in order of priority:</p>
 * <ol>
 * <li>The channel name from the metadata</li>
 * <li>red, green or blue if it's RGB</li>
 * <li>chnl-%d where %d is the index of the channel</li>
 * </ol>
 * 
 * @author Owen Feehan
 *
 */
public class ImgChnlMapAutoname extends ImgChnlMapCreator {

	private static final String[] RGB_CHNL_NAMES = {"red", "green", "blue" };
	
	public ImgChnlMapAutoname() {
		super();
	}

	@Override
	public ImgChnlMap createMap(OpenedRaster openedRaster) throws CreateException {

		ImgChnlMap map = new ImgChnlMap();

		// null indicates that there are no names
		Optional<List<String>> names = openedRaster.channelNames();
		
		try {
			boolean rgb = openedRaster.isRGB() && openedRaster.numChnl()==3;
			
			// The insertion order is critical here to remember R, G, B
			for (int c = 0; c < openedRaster.numChnl(); c++) {
				map.add(
					new ImgChnlMapEntry(
						nameFor(c, names, rgb),
						c
					)
				);
			}

		} catch (RasterIOException e) {
			throw new CreateException(e);
		}

		return map;
	}
	
	private String nameFor( int c, Optional<List<String>> names, boolean rgb ) {
		if (names.isPresent()) {
			return names.get().get(c);
		} else if (rgb) {
			return rgbNameFor(c);
		} else {
			return String.format("chnl-%d", c);
		}
	}
	
	private String rgbNameFor( int c ) {
		if (c<3) {
			return RGB_CHNL_NAMES[c];
		} else {
			assert(false);
			return "name-should-never-occur";
		}
	}
}
