package org.anchoranalysis.plugin.mpp.experiment.feature;

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

	@FunctionalInterface
	public static interface ExtractObj {
		ObjMask extractObj(FeatureObjMaskPairMergedParams params);
	}
	
	// Create new more specific params
	public void calcAndInsert( FeatureObjMaskPairMergedParams params, ExtractObj extractObj, FeatureCalculatorMulti<FeatureObjMaskParams> session ) throws FeatureCalcException {
		FeatureObjMaskParams paramsSpecific = createNewSpecificParams(params,extractObj);
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

	private static FeatureObjMaskParams createNewSpecificParams( FeatureObjMaskPairMergedParams params, ExtractObj extractObj ) {
		FeatureObjMaskParams paramsOut = new FeatureObjMaskParams( extractObj.extractObj(params) );
		paramsOut.setNrgStack( params.getNrgStack() );
		return paramsOut;
	}

	public ResultsVector getResultsVector() {
		return out;
	}
}
