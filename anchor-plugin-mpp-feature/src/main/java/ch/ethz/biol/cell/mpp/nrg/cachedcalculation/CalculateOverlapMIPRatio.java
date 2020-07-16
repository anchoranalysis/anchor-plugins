/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;

@EqualsAndHashCode(callSuper = true)
public class CalculateOverlapMIPRatio extends CalculateOverlapMIPBase {

    // Constructor
    public CalculateOverlapMIPRatio(int regionID) {
        super(regionID);
    }

    @Override
    protected Double calculateOverlapResult(double overlap, MaxIntensityProjectionPair pair) {
        if (overlap == 0) {
            return 0.0;
        }

        int minArea = pair.minArea();
        return overlap / minArea;
    }
}
