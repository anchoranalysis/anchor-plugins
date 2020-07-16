/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;

/**
 * Extracts the gradient
 *
 * @author Owen Feehan
 */
public class ChnlProviderGradientSingleDimension extends ChnlProviderGradientBase {

    // START BEAN
    /** Which axis? X=0, Y=1, Z=2 */
    @BeanField private int axis = 0;
    // END BEAN

    @Override
    protected boolean[] createDimensionArr() throws CreateException {
        switch (axis) {
            case 0:
                return new boolean[] {true, false, false};
            case 1:
                return new boolean[] {false, true, false};
            case 2:
                return new boolean[] {false, false, true};
            default:
                throw new CreateException("Axis must be 0 (x-axis) or 1 (y-axis) or 2 (z-axis)");
        }
    }

    public int getAxis() {
        return axis;
    }

    public void setAxis(int axis) {
        this.axis = axis;
    }
}
