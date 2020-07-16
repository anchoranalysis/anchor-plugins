/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateErosion;

public class Erode extends DerivedObject {

    // START BEAN PROPERTIES
    @BeanField private int iterations;

    @BeanField private boolean do3D = true;
    // END BEAN PROPERTIES

    @Override
    protected FeatureCalculation<ObjectMask, FeatureInputSingleObject>
            createCachedCalculationForDerived(
                    CalculationResolver<FeatureInputSingleObject> session) {
        return CalculateErosion.of(session, iterations, do3D);
    }

    @Override
    public ChildCacheName cacheName() {
        return new ChildCacheName(Erode.class, iterations + "_" + do3D);
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public boolean isDo3D() {
        return do3D;
    }

    public void setDo3D(boolean do3D) {
        this.do3D = do3D;
    }
}
