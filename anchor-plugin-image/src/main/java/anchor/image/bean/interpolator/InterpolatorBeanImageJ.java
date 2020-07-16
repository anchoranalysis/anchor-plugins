/* (C)2020 */
package anchor.image.bean.interpolator;

import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.interpolator.InterpolatorImageJ;

public class InterpolatorBeanImageJ extends InterpolatorBean {

    @Override
    public Interpolator create() {
        return new InterpolatorImageJ();
    }
}
