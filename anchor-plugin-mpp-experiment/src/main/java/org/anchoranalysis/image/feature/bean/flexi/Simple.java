package org.anchoranalysis.image.feature.bean.flexi;

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
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.feature.flexi.FeatureSessionFlexiFeatureTable;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

/**
 * Simply selects features directly from the list, and objects directly from the list passed.
 * 
 * @author Owen Feehan
 *
 */
public class Simple extends FlexiFeatureTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	public FeatureSessionFlexiFeatureTable createFeatures(
			List<NamedBean<FeatureListProvider>> list) throws CreateException {
		NamedFeatureStore namedFeatures = NamedFeatureStoreFactory.createNamedFeatureList(list);
		return new FeatureSessionNamedFeatureStore(namedFeatures);
	}
	
	public static FeatureInitParams createInitParams( ImageInitParams so, NRGStack nrgStack, KeyValueParams keyValueParams ) {
		FeatureInitParams params;
		if (so!=null) {
			params = new FeatureInitParamsImageInit( so );
			params.setKeyValueParams(keyValueParams);
		} else {
			params = new FeatureInitParams( keyValueParams );
		}
		params.setNrgStack(nrgStack);
		return params;
	}



	


	@Override
	public List<FeatureCalcParams> createListCalcParams(ObjMaskCollection objs,
			NRGStackWithParams nrgStack, LogErrorReporter logErrorReporter) throws CreateException {

		List<FeatureCalcParams> out = new ArrayList<>();
		
		for( ObjMask om : objs ) {

			FeatureObjMaskParams params = new FeatureObjMaskParams(om);
			params.setNrgStack(nrgStack);
			out.add(params);
		}
		
		return out;
	}



}
