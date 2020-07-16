/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.EllipsoidUtilities;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class EllipsoidBoundsCheck extends CheckMark {

    // START BEAN PROPERTIES

    // END BEAN PROPERTIES

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {

        try {
            MarkEllipsoid me = (MarkEllipsoid) mark;

            double minBound =
                    getInitializationParameters()
                            .getMarkBounds()
                            .getMinResolved(nrgStack.getDimensions().getRes(), true);
            double maxBound =
                    getInitializationParameters()
                            .getMarkBounds()
                            .getMaxResolved(nrgStack.getDimensions().getRes(), true);

            double[] normalisedRadii =
                    EllipsoidUtilities.normalisedRadii(me, nrgStack.getDimensions().getRes());

            for (int i = 0; i < 3; i++) {
                if (normalisedRadii[i] < minBound) {
                    return false;
                }
                if (normalisedRadii[i] > maxBound) {
                    return false;
                }
            }

        } catch (NamedProviderGetException e) {
            throw new CheckException("Cannot establish bounds", e.summarize());
        }

        return true;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipsoid;
    }
}
