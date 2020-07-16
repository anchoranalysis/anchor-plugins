/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class StdDev extends FeatureResultsFromIndex {

    @Override
    protected double calcStatisticFromFeatureVal(DoubleArrayList featureVals) {
        double sum = Descriptive.sum(featureVals);
        double var =
                Descriptive.variance(
                        featureVals.size(), sum, Descriptive.sumOfSquares(featureVals));
        return Descriptive.standardDeviation(var);
    }
}
