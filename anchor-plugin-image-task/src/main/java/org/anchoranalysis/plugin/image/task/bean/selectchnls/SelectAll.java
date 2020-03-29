package org.anchoranalysis.plugin.image.task.bean.selectchnls;

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

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.image.task.grouped.ChnlSource;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/**
 * Selects all possible channels from all possible stacks
 * 
 * If a stack has a single-channel, it it uses this name as an output
 * If a stack has multiple channels, this name is used but suffixed with a number of each channel (00, 01 etc.)
 * 
 * @author FEEHANO
 *
 */
public class SelectAll extends SelectChnlsFromStacks {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public List<NamedChnl> selectChnls( ChnlSource source, boolean checkType ) throws OperationFailedException {
		
		List<NamedChnl> out = new ArrayList<>();
		
		for( String key : source.getStackStore().keys() ) {
				
			out.addAll(
				extractAllChnls(source, key, checkType)	
			);
		}
		
		return out;
	}
	
	private List<NamedChnl> extractAllChnls( ChnlSource source, String stackName, boolean checkType ) throws OperationFailedException {
		
		try {
			// We make a single histogram
			Stack stack = source.getStackStore().getException(stackName);
			
			List<NamedChnl> out = new ArrayList<>();
			for( int i=0; i<stack.getNumChnl(); i++) {
				
				String outputName = stackName + createSuffix(i, stack.getNumChnl()>1 );
				
				out.add(
					new NamedChnl(
						outputName,
						source.extractChnl(stack, checkType, i)
					)
				);
			}
			return out;
			
		} catch ( NamedProviderGetException e) {
			throw new OperationFailedException(e.summarize());
		}			
	}


	private static String createSuffix( int index, boolean hasMultipleChnls ) {
		if (hasMultipleChnls) {
			return String.format("%02d", index);
		} else {
			return "";
		}
	}
}
