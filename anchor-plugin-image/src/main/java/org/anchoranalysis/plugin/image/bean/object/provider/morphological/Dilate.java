package org.anchoranalysis.plugin.image.bean.object.provider.morphological;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;
import lombok.Getter;
import lombok.Setter;

/**
 * Dilates each object in the collection, growing bounding-boxes as necessary in relevant dimensions.
 * 
 * @author Owen Feehan
 *
 */
public class Dilate extends ObjectCollectionProviderMorphological {
	
	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private boolean bigNghb = false;
	// END BEAN PROPERTIES

	@Override
	protected ObjectMask applyMorphologicalOperation(ObjectMask object, Optional<Extent> extent) throws CreateException {
		return MorphologicalDilation.createDilatedObject(
			object,
			extent,
			isDo3D(),
			getIterations(),
			bigNghb
		);
	}
}