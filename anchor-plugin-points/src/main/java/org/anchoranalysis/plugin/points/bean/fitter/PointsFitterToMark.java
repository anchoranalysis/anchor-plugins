/* (C)2020 */
package org.anchoranalysis.plugin.points.bean.fitter;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.points.PointsBean;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;

public class PointsFitterToMark extends PointsBean<PointsFitterToMark> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PointsFitter pointsFitter;

    @BeanField @Getter @Setter private ImageDimProvider dim;

    /** If an object has fewer points than before being fitted, we ignore */
    @BeanField @Positive @Getter @Setter private int minNumPoints = 1;

    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    public void fitPointsToMark(List<Point3f> pointsForFitter, Mark mark, ImageDimensions dim)
            throws OperationFailedException {
        try {
            pointsFitter.fit(pointsForFitter, mark, dim);
        } catch (PointsFitterException | InsufficientPointsException e) {
            throw new OperationFailedException(e);
        }
    }

    public ObjectCollection createObjects() throws CreateException {
        return objects.create();
    }

    public ImageDimensions createDim() throws CreateException {
        return dim.create();
    }
}
