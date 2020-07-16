/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;

import org.anchoranalysis.anchor.mpp.feature.bean.results.FeatureResults;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;

public class Size extends FeatureResults {

    @Override
    public double calc(FeatureInputResults params) throws FeatureCalcException {
        return params.getResultsVectorCollection().size();
    }
}
