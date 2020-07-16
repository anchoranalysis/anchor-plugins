/* (C)2020 */
package ch.ethz.biol.cell.mpp.bound;

import java.util.Optional;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.ResolvedBound;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.math.rotation.RotationMatrix;

@NoArgsConstructor
public class ConstantBoundGenerator extends BoundCalculator {

    // START BEAN PROPERTIES
    @BeanField private ResolvedBound constantBound;
    // END BEAN PROPERTIES

    public ConstantBoundGenerator(ResolvedBound constantBound) {
        super();
        this.constantBound = constantBound;
    }

    @Override
    public BidirectionalBound calcBound(Point3d point, RotationMatrix rotMatrix) {
        return new BidirectionalBound(Optional.of(constantBound), Optional.of(constantBound));
    }
}
