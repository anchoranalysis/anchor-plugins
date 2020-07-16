/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;

public class PixelScoreIdentityImposeValueRelationToHistogram extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int nrgChnlIndexCheck = 0;

    @BeanField @Getter @Setter private int nrgChnlIndexFail = 0;

    @BeanField @Getter @Setter private int histIndex = 0;

    @BeanField @Getter @Setter private RelationBean relation;

    @BeanField @Getter @Setter private double value = 0;

    @BeanField @Getter @Setter private boolean max = true; // We use the max, otherwise the min
    // END BEAN PROPERTIES

    private int histMax;

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {

        double pxlValue = pixelVals[nrgChnlIndexCheck];

        if (relation.create().isRelationToValueTrue(pxlValue, histMax)) {
            return value;
        }
        return pixelVals[nrgChnlIndexFail];
    }

    @Override
    public void init(List<Histogram> histograms, Optional<KeyValueParams> keyValueParams)
            throws InitException {
        try {
            if (max) {
                histMax = histograms.get(histIndex).calcMax();
            } else {
                histMax = histograms.get(histIndex).calcMin();
            }
        } catch (OperationFailedException e) {
            throw new InitException(e);
        }
    }
}
