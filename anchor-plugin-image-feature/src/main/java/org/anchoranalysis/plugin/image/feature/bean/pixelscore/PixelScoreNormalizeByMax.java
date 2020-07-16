/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * This assumes the histograms correspond to the nrg channels exactly (in terms of indexing)
 *
 * @author Owen Feehan
 */
public class PixelScoreNormalizeByMax extends PixelScoreSingleChnl {

    private double maxEdge;

    @Override
    public void init(List<Histogram> histograms, Optional<KeyValueParams> keyValueParams)
            throws InitException {
        try {
            maxEdge = histograms.get(getNrgChnlIndex()).calcMax();
        } catch (OperationFailedException e) {
            throw new InitException(e);
        }
    }

    @Override
    protected double deriveScoreFromPixelVal(int pixelVal) {
        return pixelVal / maxEdge;
    }
}
