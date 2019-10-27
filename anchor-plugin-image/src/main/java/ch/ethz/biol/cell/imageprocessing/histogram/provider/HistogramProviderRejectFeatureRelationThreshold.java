package ch.ethz.biol.cell.imageprocessing.histogram.provider;

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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorRes;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSingle;
import org.anchoranalysis.image.histogram.Histogram;

// Rejects a histogram if a feature relation holds
public class HistogramProviderRejectFeatureRelationThreshold extends HistogramProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private HistogramProvider histogramProvider;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField
	private FeatureEvaluatorRes featureEvaluator;
	
	@BeanField
	private double threshold;
	// END BEAN PROPERTIES

	@Override
	public Histogram create() throws CreateException {
	
		Histogram h = histogramProvider.create();
		
		FeatureSessionCreateParamsSingle session;
		try {
			session = featureEvaluator.createAndStartSession();
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		
		try {
			double featureVal = session.calc(h);
	
			//System.out.printf("Is feature %s=%f %s %f???\n", featureEvaluator.featureName(), featureVal, relation.getName() ,threshold );
			
			if (relation.create().isRelationToValueTrue(featureVal, threshold)) {
				//System.out.println("rejected");
				throw new CreateException( String.format("Feature rejected as %s=%f %s %f", session.featureName(), featureVal, relation.getBeanName() ,threshold ));
			}
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
		
		return h;
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	public FeatureEvaluatorRes getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluatorRes featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

}
