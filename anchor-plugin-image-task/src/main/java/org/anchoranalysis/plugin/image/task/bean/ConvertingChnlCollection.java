package org.anchoranalysis.plugin.image.task.bean;

/*-
 * #%L
 * anchor-plugin-image-task
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

import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.chnl.ChnlGetter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;

class ConvertingChnlCollection implements ChnlGetter {

	private ChnlGetter source;
	private ChnlConverter<?> chnlConverter;
	private ConversionPolicy conversionPolicy;
	
	public ConvertingChnlCollection(ChnlGetter source, ChnlConverter<?> chnlConverter, ConversionPolicy changeExisting) {
		super();
		this.source = source;
		this.chnlConverter = chnlConverter;
		this.conversionPolicy = changeExisting;
	}

	@Override
	public boolean hasChnl(String chnlName) {
		return source.hasChnl(chnlName);
	}

	@Override
	public Chnl getChnl(String chnlName, int t, ProgressReporter progressReporter) throws RasterIOException,
			GetOperationFailedException {
		
		Chnl chnl = source.getChnl(chnlName, t, progressReporter);
		return chnlConverter.convert(chnl, conversionPolicy);
	}

}
