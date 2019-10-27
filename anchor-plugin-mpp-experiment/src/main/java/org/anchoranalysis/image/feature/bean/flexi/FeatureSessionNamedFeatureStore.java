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

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.flexi.FeatureSessionFlexiFeatureTable;
import org.anchoranalysis.image.init.ImageInitParams;

class FeatureSessionNamedFeatureStore extends FeatureSessionFlexiFeatureTable {

	private SequentialSession session;

	private NamedFeatureStore namedFeatureStore;
	
	public FeatureSessionNamedFeatureStore(NamedFeatureStore namedFeatureStore) {
		this.namedFeatureStore = namedFeatureStore;
		session = new SequentialSession( namedFeatureStore.listFeatures() );
	}

	@Override
	public void start(ImageInitParams soImage, SharedFeaturesInitParams soFeature, NRGStackWithParams nrgStack, LogErrorReporter logErrorReporter) throws InitException {
		
		// TODO temporarily disabled
		SharedFeatureSet sharedFeatures = createSharedFeatures(soFeature);
		//SharedFeatureSet sharedFeatures = new SharedFeatureSet();
		
		// Init all the features
		FeatureInitParams featureInitParams = Simple.createInitParams(soImage,nrgStack.getNrgStack(), nrgStack.getParams());
		//namedFeatureStore.copyTo(out);
		session.start(featureInitParams, sharedFeatures, logErrorReporter);
	}
	
	@Override
	public FeatureSessionFlexiFeatureTable duplicateForNewThread() {
		return new FeatureSessionNamedFeatureStore(namedFeatureStore.deepCopy());
	}

	@Override
	public ResultsVector calcMaybeSuppressErrors(FeatureCalcParams params, ErrorReporter errorReporter) throws FeatureCalcException {
		return session.calcSuppressErrors( params, errorReporter );
	}
	
	@Override
	public FeatureNameList createFeatureNames() {
		return namedFeatureStore.createFeatureNames();
	}

	@Override
	public int size() {
		return namedFeatureStore.size();
	}
	
	private SharedFeatureSet createSharedFeatures( SharedFeaturesInitParams soFeature ) {
		SharedFeatureSet out = new SharedFeatureSet();
		out.addDuplicate( soFeature.getSharedFeatureSet() );
		namedFeatureStore.copyToDuplicate(out.getSet());
		return out;
	}
	
}
