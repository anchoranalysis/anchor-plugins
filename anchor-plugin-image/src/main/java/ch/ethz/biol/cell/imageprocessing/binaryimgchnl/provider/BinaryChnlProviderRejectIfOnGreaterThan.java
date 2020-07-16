/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;

public class BinaryChnlProviderRejectIfOnGreaterThan extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private double maxRatioOn;

    @BeanField
    private boolean bySlice = false; // Rejects if any slice has more On than the maxRatio allows
    // END BEAN PROPERTIES

    @Override
    public Mask createFromChnl(Mask binaryImgChnl) throws CreateException {
        if (bySlice) {
            for (int z = 0; z < binaryImgChnl.getDimensions().getZ(); z++) {
                testChnl(binaryImgChnl.extractSlice(z));
            }
        } else {
            testChnl(binaryImgChnl);
        }
        return binaryImgChnl;
    }

    private void testChnl(Mask binaryImgChnl) throws CreateException {
        int cnt = binaryImgChnl.countHighValues();
        long volume = binaryImgChnl.getDimensions().getVolume();

        double ratio = ((double) cnt) / volume;

        if (ratio > maxRatioOn) {
            throw new CreateException(
                    String.format(
                            "binaryImgChnl has on-pixel ratio of %f when a max of %f is allowed",
                            ratio, maxRatioOn));
        }
    }

    public double getMaxRatioOn() {
        return maxRatioOn;
    }

    public void setMaxRatioOn(double maxRatioOn) {
        this.maxRatioOn = maxRatioOn;
    }

    public boolean isBySlice() {
        return bySlice;
    }

    public void setBySlice(boolean bySlice) {
        this.bySlice = bySlice;
    }
}
