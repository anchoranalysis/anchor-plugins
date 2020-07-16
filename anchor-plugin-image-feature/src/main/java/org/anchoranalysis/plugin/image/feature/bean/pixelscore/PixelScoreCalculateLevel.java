/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.histogram.Histogram;

public class PixelScoreCalculateLevel extends PixelScoreCalculateLevelBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double distanceMax = 20;
    // END BEAN PROPERTIES

    private double distanceMaxDivider;

    @Override
    protected void beforeCalcSetup(Histogram hist, int level) {
        // We divide by twice the distanceMax so we always get a figure bounded [0,0.5]
        distanceMaxDivider = distanceMax * 2;
    }

    @Override
    protected double calcForPixel(int pxlValue, int level) {

        if (pxlValue < level) {

            int diff = level - pxlValue;

            if (diff > distanceMax) {
                return 0;
            }

            double mem = diff / distanceMaxDivider;
            return 0.5 - mem;
        } else {
            int diff = pxlValue - level;

            if (diff > distanceMax) {
                return 1;
            }

            double mem = diff / distanceMaxDivider;
            return 0.5 + mem;
        }
    }
}
