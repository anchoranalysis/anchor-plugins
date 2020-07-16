/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.surface;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public abstract class SurfaceNumberVoxelsBase extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField private boolean mip = false;

    @BeanField private boolean suppress3D = false;
    /// END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
        return input.calc(createParams(mip, suppress3D));
    }

    protected abstract FeatureCalculation<Integer, FeatureInputSingleObject> createParams(
            boolean mip, boolean suppress3d);

    public boolean isMip() {
        return mip;
    }

    public void setMip(boolean mip) {
        this.mip = mip;
    }

    public boolean isSuppress3D() {
        return suppress3D;
    }

    public void setSuppress3D(boolean suppress3d) {
        suppress3D = suppress3d;
    }
}
