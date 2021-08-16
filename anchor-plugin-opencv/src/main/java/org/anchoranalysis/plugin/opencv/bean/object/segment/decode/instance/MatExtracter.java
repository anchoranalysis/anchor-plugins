package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.opencv.core.Mat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MatExtracter {

    /** Extracts an array of floats from a matrix */
    public static double[] extractDoubleArray(Mat mat, int rowIndex, int arrSize) {
        double[] arr = new double[arrSize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }

    /** Extracts an array of floats from a matrix */
    public static float[] extractFloatArray(Mat mat, int rowIndex, int arrSize) {
        float[] arr = new float[arrSize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }

    /** Extracts an array of bytes from a matrix */
    public static byte[] extractByteArray(Mat mat, int rowIndex, int arrSize) {
        byte[] arr = new byte[arrSize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }

    /** Extracts an array of ints from a matrix */
    public static int[] extractIntArray(Mat mat, int rowIndex, int arrSize) {
        int[] arr = new int[arrSize];
        mat.get(rowIndex, 0, arr);
        return arr;
    }
}
