package org.anchoranalysis.plugin.image.task.bean.chnl.conversionstyle;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.Set;
import java.util.function.BiConsumer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.image.task.chnl.convert.ChnlGetterForTimepoint;

public class IndependentChnls extends ChnlConversionStyle {

	// START BEAN PROPERTIES
	/** Iff TRUE and we cannot find a channel in the file, we ignore it and carry on */
	@BeanField
	private boolean ignoreMissingChnl = true;
	// END BEAN PROPERTIES
	
	public void convert(
		Set<String> chnlNames,
		ChnlGetterForTimepoint chnlGetter,
		BiConsumer<String, Stack> stacksOut,
		LogErrorReporter logErrorReporter
	) throws AnchorIOException {
		
		for( String key : chnlNames ) {
			
			try {
				Channel chnl = chnlGetter.getChnl(key);
				stacksOut.accept(
					key,
					new Stack(chnl)
				);
			} catch (GetOperationFailedException e) {
				if (ignoreMissingChnl) {
					logErrorReporter.getLogReporter().logFormatted("Cannot open channel '%s'. Ignoring.", key);
					continue;
				} else {
					throw new AnchorIOException( String.format("Cannot open channel '%s'.", key), e );
				}
			}
			
		}
	}

	public boolean isIgnoreMissingChnl() {
		return ignoreMissingChnl;
	}

	public void setIgnoreMissingChnl(boolean ignoreMissingChnl) {
		this.ignoreMissingChnl = ignoreMissingChnl;
	}
}
