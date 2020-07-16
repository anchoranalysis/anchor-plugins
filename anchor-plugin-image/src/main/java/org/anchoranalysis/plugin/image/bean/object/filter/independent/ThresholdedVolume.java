package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolume;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterRelation;

/**
 * Only keeps objects whose feature-value satisfies a condition relative to a threshold.
 * 
 * <p>Specifically, <code>relation(volume,threshold)</code> must be true.
 * 
 * @author Owen Feehan
 *
 */
public class ThresholdedVolume extends ObjectFilterRelation {

	// START BEAN PROPERTIES
	@BeanField
	private UnitValueVolume threshold;
	// END BEAN PROPERTIES
	
	private int thresholdResolved;
	
	@Override
	protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter) throws OperationFailedException {
		super.start(dim, objectsToFilter);
		thresholdResolved = resolveThreshold(dim);
	}

	@Override
	protected boolean match(ObjectMask object, Optional<ImageDimensions> dim, RelationToValue relation) {
		return relation.isRelationToValueTrue( object.numberVoxelsOn(), thresholdResolved);
	}

	private int resolveThreshold( Optional<ImageDimensions> dim ) throws OperationFailedException {
		try {
			return (int) Math.floor(
				threshold.resolveToVoxels(
					dim.map(ImageDimensions::getRes)
				)
			);
		} catch (UnitValueException e) {
			throw new OperationFailedException(e);
		}
	}
}