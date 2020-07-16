/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.direction;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.image.bean.orientation.DirectionVectorBean;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

public abstract class FeatureMarkDirection extends FeatureMark {

    // START BEAN PROPERTIES
    @BeanField private DirectionVectorBean directionVector;
    // END BEAN PROPERTIES

    private DirectionVector dv;

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);
        dv = directionVector.createVector();
    }

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

        if (!(input.get().getMark() instanceof MarkEllipsoid)) {
            throw new FeatureCalcException("Only supports MarkEllipsoids");
        }

        MarkEllipsoid mark = (MarkEllipsoid) input.get().getMark();

        Orientation orientation = mark.getOrientation();
        RotationMatrix rotMatrix = orientation.createRotationMatrix();
        return calcForEllipsoid(mark, orientation, rotMatrix, dv.createVector3d());
    }

    protected abstract double calcForEllipsoid(
            MarkEllipsoid mark,
            Orientation orientation,
            RotationMatrix rotMatrix,
            Vector3d directionVector)
            throws FeatureCalcException;

    public DirectionVectorBean getDirectionVector() {
        return directionVector;
    }

    public void setDirectionVector(DirectionVectorBean directionVector) {
        this.directionVector = directionVector;
    }
}
