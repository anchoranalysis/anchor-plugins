/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity.gradient;

import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public abstract class IntensityGradientBase extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @NonNegative private int nrgIndexX = -1;

    @BeanField @NonNegative private int nrgIndexY = -1;

    @BeanField @OptionalBean private int nrgIndexZ = -1;

    @BeanField private int subtractConstant = 0;
    // END BEAN PROPERTIES

    protected FeatureCalculation<List<Point3d>, FeatureInputSingleObject> gradientCalculation() {
        return new CalculateGradientFromMultipleChnls(
                nrgIndexX, nrgIndexY, nrgIndexZ, subtractConstant);
    }

    public int getNrgIndexX() {
        return nrgIndexX;
    }

    public void setNrgIndexX(int nrgIndexX) {
        this.nrgIndexX = nrgIndexX;
    }

    public int getNrgIndexY() {
        return nrgIndexY;
    }

    public void setNrgIndexY(int nrgIndexY) {
        this.nrgIndexY = nrgIndexY;
    }

    public int getNrgIndexZ() {
        return nrgIndexZ;
    }

    public void setNrgIndexZ(int nrgIndexZ) {
        this.nrgIndexZ = nrgIndexZ;
    }

    public int getSubtractConstant() {
        return subtractConstant;
    }

    public void setSubtractConstant(int subtractConstant) {
        this.subtractConstant = subtractConstant;
    }
}
