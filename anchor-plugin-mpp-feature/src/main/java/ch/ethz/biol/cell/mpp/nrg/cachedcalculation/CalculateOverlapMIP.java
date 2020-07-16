/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;

@EqualsAndHashCode(callSuper = true)
public class CalculateOverlapMIP extends CalculateOverlapMIPBase {

    public CalculateOverlapMIP(int regionID) {
        super(regionID);
    }

    @Override
    protected Double calculateOverlapResult(double overlap, MaxIntensityProjectionPair pair) {
        return overlap;
    }
}
