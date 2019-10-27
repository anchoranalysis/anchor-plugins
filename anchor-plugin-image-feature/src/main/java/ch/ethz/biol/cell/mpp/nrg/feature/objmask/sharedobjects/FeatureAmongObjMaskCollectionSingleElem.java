package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.NullLogReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.session.SequentialSessionSingleFeature;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;

public abstract class FeatureAmongObjMaskCollectionSingleElem extends FeatureAmongObjMaskCollection {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	// START BEAN PROPERTIES
	@BeanField
	private Feature item;
	// END BEAN PROPERTIES
		
	
	private SequentialSessionSingleFeature sessionObjs;
	
	@Override
	public void beforeCalcCast(FeatureInitParamsImageInit params,
			FeatureSessionCacheRetriever session) throws InitException {
		super.beforeCalcCast(params, session);
		
		sessionObjs = new SequentialSessionSingleFeature(item);
		
		//SharedFeatureSet sharedFeatures = session.getSharedFeatureList().duplicate();
		SharedFeatureSet sharedFeatures = new SharedFeatureSet();
		
		// We don't log these feature calculations 
		sessionObjs.start(params, sharedFeatures, new LogErrorReporter( new NullLogReporter() ) );
	}
	
	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

	protected SequentialSessionSingleFeature getSessionObjs() {
		return sessionObjs;
	}
}
