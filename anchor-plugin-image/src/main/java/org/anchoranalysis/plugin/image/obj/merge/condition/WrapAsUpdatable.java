package org.anchoranalysis.plugin.image.obj.merge.condition;

import java.util.Optional;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Wraps a BeforeCondition as an UpdatableBefoerCondition
 * 
 * @author Owen Feehan
 *
 */
public class WrapAsUpdatable implements UpdatableBeforeCondition {

	private BeforeCondition beforeCondition;
	
	// TEMPORARILY UPDATED
	private ObjectMask omSrc;
	private Optional<ImageResolution> res;
	
	public WrapAsUpdatable(BeforeCondition beforeCondition) {
		super();
		this.beforeCondition = beforeCondition;
	}

	@Override
	public void updateSrcObj(ObjectMask omSrc, Optional<ImageResolution> res) throws OperationFailedException {
		this.omSrc = omSrc;
		this.res = res;
	}

	@Override
	public boolean accept(ObjectMask omDest) throws OperationFailedException {
		return beforeCondition.accept(omSrc, omDest, res);
	}
}
