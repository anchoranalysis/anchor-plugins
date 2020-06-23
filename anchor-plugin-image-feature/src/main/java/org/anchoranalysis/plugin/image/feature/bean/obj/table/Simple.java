package org.anchoranalysis.plugin.image.feature.bean.obj.table;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.session.FeatureTableSession;
import org.anchoranalysis.image.feature.session.NamedFeatureStoreSession;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;

/**
 * Simply selects features directly from the list, and objects directly from the list passed.
 * 
 * @author Owen Feehan
 *
 */
public class Simple extends FeatureTableObjs<FeatureInputSingleObj> {

	@Override
	public FeatureTableSession<FeatureInputSingleObj> createFeatures(
			List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> list,
			NamedFeatureStoreFactory storeFactory,
			boolean suppressErrors
	) throws CreateException {
		NamedFeatureStore<FeatureInputSingleObj> namedFeatures = storeFactory.createNamedFeatureList(list);
		return new NamedFeatureStoreSession(namedFeatures);
	}

	@Override
	public List<FeatureInputSingleObj> createListInputs(ObjectCollection objs,
			NRGStackWithParams nrgStack, LogErrorReporter logErrorReporter) throws CreateException {

		List<FeatureInputSingleObj> out = new ArrayList<>();
		
		for( ObjectMask om : objs ) {
			checkObjInsideScene(om, nrgStack.getDimensions().getExtnt());

			out.add(
				new FeatureInputSingleObj(om, nrgStack)
			);
		}
		
		return out;
	}
	
	private static void checkObjInsideScene( ObjectMask om, Extent extent) throws CreateException {
		if (!extent.contains(om.getBoundingBox())) {
			throw new CreateException(
				String.format(
					"Object is not (perhaps fully) contained inside the scene: %s is not in %s",
					om.getBoundingBox(),
					extent
				)
			);
		}
	}
}
