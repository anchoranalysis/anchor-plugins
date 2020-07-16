/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;

public class Scale extends ScalarProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScalarProposer scalarProposer;

    @BeanField @Getter @Setter private double factor = 1.0;
    // END BEAN PROPERTIES

    @Override
    public double propose(RandomNumberGenerator randomNumberGenerator, ImageResolution res)
            throws OperationFailedException {
        return scalarProposer.propose(randomNumberGenerator, res) * factor;
    }
}
