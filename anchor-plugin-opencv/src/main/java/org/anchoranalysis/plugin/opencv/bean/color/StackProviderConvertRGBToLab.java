/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.color;

import org.opencv.imgproc.Imgproc;

public class StackProviderConvertRGBToLab extends StackProviderCVColorConverter {

    @Override
    protected int colorSpaceCode() {
        return Imgproc.COLOR_BGR2Lab;
    }
}
