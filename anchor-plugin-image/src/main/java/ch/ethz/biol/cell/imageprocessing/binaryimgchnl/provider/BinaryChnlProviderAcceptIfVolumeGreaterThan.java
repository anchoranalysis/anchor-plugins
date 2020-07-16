/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;

public class BinaryChnlProviderAcceptIfVolumeGreaterThan extends BinaryChnlProviderReceive {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double minRatio = 0.01;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromChnlReceive(Mask larger, Mask smaller) throws CreateException {
        int countLarger = larger.countHighValues();
        int countSmaller = smaller.countHighValues();

        double ratio = ((double) countSmaller) / countLarger;

        if (ratio > minRatio) {
            return smaller;
        } else {
            return larger;
        }
    }
}
