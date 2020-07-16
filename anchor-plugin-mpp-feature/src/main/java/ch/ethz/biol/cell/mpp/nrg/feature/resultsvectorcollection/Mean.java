/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class Mean extends FeatureResultsFromIndex {

    @Override
    protected double calcStatisticFromFeatureVal(DoubleArrayList featureVals)
            throws FeatureCalcException {
        return Descriptive.mean(featureVals);
    }
}
