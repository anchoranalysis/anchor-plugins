/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.slice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ArraySearchUtilities {

    public static int findIndexOfMaximum(double[] arr) {
        double max = Double.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < arr.length; i++) {
            double val = arr[i];
            if (val > max) {
                maxIndex = i;
                max = val;
            }
        }
        return maxIndex;
    }

    public static int findIndexOfMinimum(double[] arr) {
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < arr.length; i++) {
            double val = arr[i];
            if (val < min) {
                minIndex = i;
                min = val;
            }
        }
        return minIndex;
    }
}
