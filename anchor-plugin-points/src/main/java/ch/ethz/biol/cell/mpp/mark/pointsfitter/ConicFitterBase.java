/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;

public abstract class ConicFitterBase extends PointsFitter {

    // START BEAN
    @BeanField @Positive private double shellRad;

    @BeanField private double subtractRadii = 0.0;

    @BeanField private double scaleRadii = 1.0;

    @BeanField private float inputPointShift = 0.0f;
    // END BEAN

    public void duplicateHelper(ConicFitterBase out) {
        out.shellRad = shellRad;
        out.subtractRadii = subtractRadii;
        out.inputPointShift = inputPointShift;
        out.scaleRadii = scaleRadii;
    }

    public double getShellRad() {
        return shellRad;
    }

    public void setShellRad(double shellRad) {
        this.shellRad = shellRad;
    }

    public double getSubtractRadii() {
        return subtractRadii;
    }

    public void setSubtractRadii(double subtractRadii) {
        this.subtractRadii = subtractRadii;
    }

    public float getInputPointShift() {
        return inputPointShift;
    }

    public void setInputPointShift(float inputPointShift) {
        this.inputPointShift = inputPointShift;
    }

    public double getScaleRadii() {
        return scaleRadii;
    }

    public void setScaleRadii(double scaleRadii) {
        this.scaleRadii = scaleRadii;
    }
}
