/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.orientation.DirectionVectorBean;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageResolution;

public class FromUnitValueDistance extends ScalarProposer {

    // START BEAN PROPERTIES
    @BeanField private UnitValueDistance unitValueDistance;

    @BeanField private DirectionVectorBean directionVector;
    // END BEAN PROPERTIES

    @Override
    public double propose(RandomNumberGenerator randomNumberGenerator, ImageResolution res)
            throws OperationFailedException {
        // TODO this could be a bit slow, we are creating an object on the heap every time from
        // directionVector
        return unitValueDistance.resolve(Optional.of(res), directionVector.createVector());
    }

    public UnitValueDistance getUnitValueDistance() {
        return unitValueDistance;
    }

    public void setUnitValueDistance(UnitValueDistance unitValueDistance) {
        this.unitValueDistance = unitValueDistance;
    }

    public DirectionVectorBean getDirectionVector() {
        return directionVector;
    }

    public void setDirectionVector(DirectionVectorBean directionVector) {
        this.directionVector = directionVector;
    }
}
