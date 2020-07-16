/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;

public class Constant extends ScalarProposer {

    // START BEAN PROPERTIES
    @BeanField private double value;
    // END BEAN PROPERTIES

    @Override
    public double propose(RandomNumberGenerator randomNumberGenerator, ImageResolution res)
            throws OperationFailedException {
        return value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
