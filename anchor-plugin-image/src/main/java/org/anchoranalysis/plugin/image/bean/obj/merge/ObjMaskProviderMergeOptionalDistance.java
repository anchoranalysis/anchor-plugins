package org.anchoranalysis.plugin.image.bean.obj.merge;



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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.plugin.image.obj.merge.condition.BeforeCondition;
import org.anchoranalysis.plugin.image.obj.merge.condition.DistanceCondition;

import lombok.Getter;
import lombok.Setter;

/**
 * An ObjMaskProviderMergeBase that optionally imposes a maximum-distance requirement between objects that are possibly merged
 *   
 * @author Owen Feehan
 *
 */
public abstract class ObjMaskProviderMergeOptionalDistance extends ObjMaskProviderMergeBase {

	// START BEAN FIELDS
	@BeanField @Getter @Setter
	private boolean suppressZ = false;
	
	/** An optional maximum distance */
	@BeanField @OptionalBean @Getter @Setter
	private UnitValueDistance maxDist; 
	// END BEAN FIELDS
	
	protected BeforeCondition maybeDistanceCondition() {
		return new DistanceCondition(getMaxDist(), suppressZ, getLogger().messageLogger() );
	}
}
