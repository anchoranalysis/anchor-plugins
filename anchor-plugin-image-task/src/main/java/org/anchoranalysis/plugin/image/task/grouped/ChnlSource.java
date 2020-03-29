package org.anchoranalysis.plugin.image.task.grouped;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;

/** Source of channels for aggregating. Checks have been applied to make sure all chnls are consistent (same dimensions, type etc.) */
public class ChnlSource {

	private NamedImgStackCollection stackStore;
	private ConsistentChnlChecker chnlChecker;
	
	public ChnlSource(NamedImgStackCollection stackStore, ConsistentChnlChecker chnlChecker) {
		super();
		this.stackStore = stackStore;
		this.chnlChecker = chnlChecker;
	}
	
	public Chnl extractChnl( String stackName, boolean checkType ) throws OperationFailedException {
		
		try {
			// We make a single histogram
			Stack stack = stackStore.getException(stackName);
				
			if (stack.getNumChnl()>1) {
				throw new OperationFailedException("Each stack may only have a single channel");
			}
			
			return extractChnl(stack, checkType, 0 );
		} catch ( NamedProviderGetException e) {
			throw new OperationFailedException(
				String.format("Cannot extract a single channel from stack %s", stackName),
				e.summarize()
			);
		}
	}
	
	public Chnl extractChnl( String stackName, boolean checkType, int index ) throws OperationFailedException {
	
		try {
			// We make a single histogram
			Stack stack = stackStore.getException(stackName);
			return extractChnl(stack, checkType, index );
			
		} catch ( NamedProviderGetException e) {
			throw new OperationFailedException(
				String.format("Cannot extract channel %d from stack %s", index, stackName),
				e.summarize()
			);
		}
	}
	
	public Chnl extractChnl( Stack stack, boolean checkType, int index ) throws OperationFailedException {
		try {
			Chnl chnl = stack.getChnl(index);
			
			if (checkType) {
				chnlChecker.checkChnlType(chnl.getVoxelDataType());
			}
			
			return chnl;
		} catch ( SetOperationFailedException e) {
			throw new OperationFailedException(
				String.format("Cannot extract channel %d from stack", index),
				e
			);
		}
	}

	public NamedImgStackCollection getStackStore() {
		return stackStore;
	}
}
