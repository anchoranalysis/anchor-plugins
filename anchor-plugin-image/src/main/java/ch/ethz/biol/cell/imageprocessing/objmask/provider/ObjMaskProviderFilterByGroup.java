package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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


import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.FunctionalUtilities;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// TODO lots of duplication with ObjMaskProviderFilter
public class ObjMaskProviderFilterByGroup extends ObjMaskProviderFilterBase {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objsGrouped;
	// END BEAN PROPERTIES
	
	@Override
	protected ObjectCollection createFromObjs(ObjectCollection in, Optional<ObjectCollection> omcRejected,
			Optional<ImageDim> dim) throws CreateException {

		ObjectCollection inGroups = objsGrouped.create();
		List<ObjWithMatches> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( inGroups, in );
		
		return new ObjectCollection(
			FunctionalUtilities.flatMapWithException(
				matchList.stream().map(ObjWithMatches::getMatches),
				CreateException.class,
				matches -> filter(matches, dim, omcRejected).asList() 
			)
		);
	}
	
	public ObjMaskProvider getObjsGrouped() {
		return objsGrouped;
	}

	public void setObjsGrouped(ObjMaskProvider objsGrouped) {
		this.objsGrouped = objsGrouped;
	}
}
