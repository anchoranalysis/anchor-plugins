/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class Sum extends FeatureResultsFromIndex {

    @Override
    protected double calcStatisticFromFeatureVal(DoubleArrayList featureVals) {
        return Descriptive.sum(featureVals);
    }
}
