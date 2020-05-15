package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;

import org.anchoranalysis.anchor.mpp.feature.bean.results.FeatureResults;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVectorCollection;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;

import cern.colt.list.DoubleArrayList;

public abstract class FeatureResultsFromIndex extends FeatureResults {

	// START BEAN PROPERTIES
	@BeanField
	private String id = "";
	// END BEAN PROPERTIES
	
	@Override
	public double calc(FeatureInputResults params) throws FeatureCalcException {

		try {
			int index = params.getFeatureNameIndex().indexOf(id);
			
			ResultsVectorCollection rvc = params.getResultsVectorCollection();
			
			if (rvc.size()==0) {
				throw new FeatureCalcException("No feature-values exist, so this operation is undefined");
			}
			
			return calcStatisticFromFeatureVal(
				arrayListFrom(rvc,index)
			);
			
		} catch (GetOperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	protected abstract double calcStatisticFromFeatureVal(DoubleArrayList featureVals) throws FeatureCalcException;
	
	private static DoubleArrayList arrayListFrom( ResultsVectorCollection rvc, int index ) {
		DoubleArrayList featureVals = new DoubleArrayList();
		for (int i=0; i<rvc.size(); i++) {
			featureVals.add(rvc.get(i).get(index));
		}
		return featureVals;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
