/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;

public class Quantile extends FeatureResultsFromIndex {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double quantile = 0; // NOSONAR

    /** If true, the quantile is interpreted as a percentage rather than a decimal */
    @BeanField @Getter @Setter private boolean asPercentage = false;
    // END BEAN PROPERTIES

    @Override
    protected double calcStatisticFromFeatureVal(DoubleArrayList featureVals) {
        featureVals.sort();

        double quantileFinal = asPercentage ? quantile / 100 : quantile;
        return Descriptive.quantile(featureVals, quantileFinal);
    }
}
