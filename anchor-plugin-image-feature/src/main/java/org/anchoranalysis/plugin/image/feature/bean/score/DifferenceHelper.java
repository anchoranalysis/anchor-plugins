package org.anchoranalysis.plugin.image.feature.bean.score;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DifferenceHelper {

    public static double differenceFromValue(
            int valueFirst,
            int valueSecond,
            double widthGreaterThan,
            double widthLessThan,
            int minDifference) {
        double diff = (double) (valueFirst - valueSecond - minDifference);

        if (diff < (-1 * widthLessThan)) {
            return 0.0;
        }

        if (diff > widthGreaterThan) {
            return 1.0;
        }

        if (diff < 0) {
            return (diff / (widthLessThan * 2)) + 0.5;
        } else {
            return (diff / (widthGreaterThan * 2)) + 0.5;
        }
    }

    public static double differenceFromParams(
            int[] pixelVals,
            int energyChannelIndexFirst,
            int energyChannelIndexSecond,
            double width,
            int minDifference) {
        return differenceFromValue(
                pixelVals[energyChannelIndexFirst],
                pixelVals[energyChannelIndexSecond],
                width,
                minDifference);
    }

    private static double differenceFromValue(
            int valFirst, int valSecond, double width, int minDifference) {
        return differenceFromValue(valFirst, valSecond, width, width, minDifference);
    }
}
