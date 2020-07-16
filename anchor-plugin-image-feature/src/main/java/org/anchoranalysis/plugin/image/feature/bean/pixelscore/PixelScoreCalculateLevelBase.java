/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;

public abstract class PixelScoreCalculateLevelBase extends PixelScoreSingleChnl {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private CalculateLevel calculateLevel;

    @BeanField @Getter @Setter private int histChnlIndex = 0;
    // END BEAN PROPERTIES

    private int level;

    @Override
    public void init(List<Histogram> histograms, Optional<KeyValueParams> keyValueParams)
            throws InitException {

        try {
            Histogram hist = histograms.get(histChnlIndex);
            level = calculateLevel.calculateLevel(hist);

            beforeCalcSetup(hist, level);
        } catch (OperationFailedException e) {
            throw new InitException(e);
        }
    }

    @Override
    protected double deriveScoreFromPixelVal(int pixelVal) {
        return calcForPixel(pixelVal, level);
    }

    protected abstract void beforeCalcSetup(Histogram hist, int level)
            throws OperationFailedException;

    protected abstract double calcForPixel(int pxlValue, int level);
}
