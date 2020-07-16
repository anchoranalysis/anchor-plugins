/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class EllipseBoundsCheck extends CheckMark {

    // START BEAN PROPERTIES
    // END BEAN PROPERTIES

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {

        MarkEllipse me = (MarkEllipse) mark;

        try {
            double minBound =
                    getInitializationParameters()
                            .getMarkBounds()
                            .getMinResolved(nrgStack.getDimensions().getRes(), false);
            double maxBound =
                    getInitializationParameters()
                            .getMarkBounds()
                            .getMaxResolved(nrgStack.getDimensions().getRes(), false);

            if (me.getRadii().getX() < minBound) {
                return false;
            }

            if (me.getRadii().getY() < minBound) {
                return false;
            }

            if (me.getRadii().getX() > maxBound) {
                return false;
            }

            return me.getRadii().getY() <= maxBound;
        } catch (NamedProviderGetException e) {
            throw new CheckException("Cannot establish bounds", e.summarize());
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipse;
    }
}
