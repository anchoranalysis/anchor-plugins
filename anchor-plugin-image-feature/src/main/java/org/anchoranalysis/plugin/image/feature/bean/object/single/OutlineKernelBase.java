package org.anchoranalysis.plugin.image.feature.bean.object.single;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.function.CheckedSupplier;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernelParameters;
import lombok.Getter;
import lombok.Setter;

public abstract class OutlineKernelBase extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean outsideAtThreshold = false;

    @BeanField @Getter @Setter private boolean ignoreAtThreshold = false;

    @BeanField @Getter @Setter private boolean do3D = false;
    // END BEAN PROPERTIES
    
    @Override
    protected double calculate(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        FeatureInputSingleObject inputSessionless = input.get();
        return calculateWithParameters(inputSessionless.getObject(), createParameters(), inputSessionless::getEnergyStackRequired);
    }
    
    protected abstract double calculateWithParameters(ObjectMask object, OutlineKernelParameters parameters, CheckedSupplier<EnergyStack,FeatureCalculationException> energyStack) throws FeatureCalculationException;
    
    private OutlineKernelParameters createParameters() {
        return new OutlineKernelParameters(outsideAtThreshold, do3D, ignoreAtThreshold);
    }
}
