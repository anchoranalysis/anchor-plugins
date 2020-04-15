package org.anchoranalysis.plugin.mpp.experiment.feature;

import java.util.function.Function;

import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;
import org.anchoranalysis.image.objmask.ObjMask;

class ResultsVectorBuilder {

	private boolean suppressErrors;
	private ErrorReporter errorReporter;
	private ResultsVector out;
	private int cnt;
		
	public ResultsVectorBuilder(int size, boolean suppressErrors, ErrorReporter errorReporter) {
		super();
		this.suppressErrors = suppressErrors;
		this.errorReporter = errorReporter;
		this.out = new ResultsVector(size);
		this.cnt = 0;
	}
	
	/** 
	 * Calculates and inserts a derived obj-mask params from a merged.
	 */
	public void calcAndInsert(
		FeatureObjMaskPairMergedParams params,
		Function<FeatureObjMaskPairMergedParams,ObjMask> extractObj,
		FeatureCalculatorMulti<FeatureObjMaskParams> session
	) throws FeatureCalcException {
		FeatureObjMaskParams paramsSpecific = new FeatureObjMaskParams(
			extractObj.apply(params)
		);
		//paramsSpecific.setNrgStack( params.getNrgStack() );  // This is necessary? Why?
		calcAndInsert(paramsSpecific, session);
	}
	
	/**
	 * Calculates the parameters belong to a particular session and inserts into a ResultsVector
	 * 
	 * @param params
	 * @param session
	 * @param start
	 * @param out
	 * @param errorReporter
	 * @return length(resultsVector)
	 * @throws FeatureCalcException
	 */
	public <T extends FeatureCalcParams> void calcAndInsert( T params, FeatureCalculatorMulti<T> session ) throws FeatureCalcException {
		ResultsVector rvImage =  suppressErrors ? session.calcOneSuppressErrors( params, errorReporter ) : session.calcOne(params) ;
		out.set(cnt, rvImage);
		cnt += rvImage.length();
	}

	public ResultsVector getResultsVector() {
		return out;
	}
}
