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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.plugin.image.obj.merge.condition.BeforeCondition;
import org.anchoranalysis.plugin.image.obj.merge.condition.DistanceCondition;

/**
 * An ObjMaskProviderMergeBase that optionally imposes a maximum-distance requirement between objects that are possibly merged
 *   
 * @author FEEHANO
 *
 */
public abstract class ObjMaskProviderMergeOptionalDistance extends ObjMaskProviderMergeBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN FIELDS

	@BeanField
	private boolean suppressZ = false;
	
	@BeanField @OptionalBean
	private ImageDimProvider resProvider;		// optionally provides a resolution
	
	@BeanField @OptionalBean
	private UnitValueDistance maxDist;			// optionally provides a maximum distance
	// END BEAN FIELDS
	
	protected ImageRes calcRes() throws OperationFailedException {
		return MergeHelpUtilities.calcRes(resProvider);
	}
	
	protected BeforeCondition maybeDistanceCondition() {
		return new DistanceCondition(getMaxDist(), suppressZ, getLogger().getLogReporter() );
	}

	public void setMaxDist(UnitValueDistance maxDist) {
		this.maxDist = maxDist;
	}

	public UnitValueDistance getMaxDist() {
		return maxDist;
	}

	public boolean isSuppressZ() {
		return suppressZ;
	}

	public void setSuppressZ(boolean suppressZ) {
		this.suppressZ = suppressZ;
	}

	public ImageDimProvider getResProvider() {
		return resProvider;
	}

	public void setResProvider(ImageDimProvider resProvider) {
		this.resProvider = resProvider;
	}
}