/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.calculation.CalculateNumVoxels;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * How many pixels are removed after a morphological closing operation on the object-mask.
 *
 * @author Owen Feehan
 */
public class NumberRemovedVoxelsAfterClosing extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int iterations = 1;

    @BeanField @Getter @Setter private boolean do3D = true;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        ObjectMask closed = input.calc(CalculateClosing.of(input.resolver(), iterations, do3D));

        double numVoxels = input.calc(new CalculateNumVoxels(false));

        return closed.numberVoxelsOn() - numVoxels;
    }
}
