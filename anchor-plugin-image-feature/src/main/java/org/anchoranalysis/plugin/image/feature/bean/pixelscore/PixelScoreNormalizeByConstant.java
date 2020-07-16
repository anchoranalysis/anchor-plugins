/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;

public class PixelScoreNormalizeByConstant extends PixelScoreSingleChnl {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double value = 255;
    // END BEAN PROPERTIES

    @Override
    protected double deriveScoreFromPixelVal(int pixelVal) {
        return pixelVal / value;
    }
}
