package org.anchoranalysis.anchor.plugin.quick.bean.input;

/*-
 * #%L
 * anchor-plugin-quick
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

import java.util.List;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.chnl.map.ImgChnlMapDefine;
import org.anchoranalysis.plugin.io.bean.input.chnl.NamedChnls;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;

/** Helps in creating NamedChnls */
class NamedChnlsCreator {

	public static NamedChnls create(
			InputManager<FileInput> files,
			String mainChnlName,
			int mainChnlIndex,
			List<ImgChnlMapEntry> additionalChnls,
			RasterReader rasterReader
	) throws BeanMisconfiguredException {
		NamedChnls namedChnls = new NamedChnls();
		namedChnls.setImgChnlMapCreator(
			createMapCreator(
				mainChnlName,
				mainChnlIndex,
				additionalChnls
			)
		);
		namedChnls.setFileInput( files );
		namedChnls.setRasterReader(rasterReader);
		return namedChnls;
	}
	
	private static ImgChnlMapCreator createMapCreator( String mainChnlName, int mainChnlIndex, List<ImgChnlMapEntry> additionalChnls) throws BeanMisconfiguredException {
		ImgChnlMapDefine define = new ImgChnlMapDefine();
		
		define.setImgChnlMap( new ImgChnlMap() );
		addChnlEntry(define, mainChnlName, mainChnlIndex);
		
		for( ImgChnlMapEntry entry : additionalChnls ) {
			
			if (entry.getIndex()==mainChnlIndex) {
				throw new BeanMisconfiguredException(
					String.format(
						"Channel '%s' for index %d is already defined as the main channel. There cannot be an additional channel.",
						mainChnlName,
						mainChnlIndex
					)	
				);
			}
			
			addChnlEntry(define, entry.getName(), entry.getIndex());	
		}
		return define;
	}
	
	private static void addChnlEntry( ImgChnlMapDefine define, String name, int index ) {
		define.getImgChnlMap().add( new ImgChnlMapEntry(name, index) );
	}
	
}