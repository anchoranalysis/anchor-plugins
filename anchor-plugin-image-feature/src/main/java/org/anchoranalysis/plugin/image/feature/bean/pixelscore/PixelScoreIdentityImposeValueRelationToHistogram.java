/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import java.util.List;
import java.util.Optional;
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
    @BeanField private int nrgChnlIndexCheck = 0;

    @BeanField private int nrgChnlIndexFail = 0;

    @BeanField private int histIndex = 0;

    @BeanField private RelationBean relation;

    @BeanField private double value = 0;

    @BeanField private boolean max = true; // We use the max, otherwise the min
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

    public int getHistIndex() {
        return histIndex;
    }

    public void setHistIndex(int histIndex) {
        this.histIndex = histIndex;
    }

    public RelationBean getRelation() {
        return relation;
    }

    public void setRelation(RelationBean relation) {
        this.relation = relation;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getNrgChnlIndexCheck() {
        return nrgChnlIndexCheck;
    }

    public void setNrgChnlIndexCheck(int nrgChnlIndexCheck) {
        this.nrgChnlIndexCheck = nrgChnlIndexCheck;
    }

    public int getNrgChnlIndexFail() {
        return nrgChnlIndexFail;
    }

    public void setNrgChnlIndexFail(int nrgChnlIndexFail) {
        this.nrgChnlIndexFail = nrgChnlIndexFail;
    }

    public boolean isMax() {
        return max;
    }

    public void setMax(boolean max) {
        this.max = max;
    }
}
