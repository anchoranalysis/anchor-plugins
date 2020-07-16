/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.init.PointsInitParams;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.extent.ImageDimensions;

public class PointsFitterReference extends PointsFitter {

    // START BEAN
    @BeanField @Getter @Setter private String id;
    // END BEAN

    private PointsFitter pointsFitter;

    @Override
    public void onInit(PointsInitParams so) throws InitException {
        super.onInit(so);
        try {
            this.pointsFitter = getInitializationParameters().getPointsFitterSet().getException(id);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return pointsFitter.isCompatibleWith(testMark);
    }

    @Override
    public void fit(List<Point3f> points, Mark mark, ImageDimensions dimensions)
            throws PointsFitterException, InsufficientPointsException {
        pointsFitter.fit(points, mark, dimensions);
    }
}
