/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever;

import java.util.List;
import org.anchoranalysis.anchor.mpp.bean.points.PointsBean;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;

public abstract class OutlinePixelsRetriever extends PointsBean<OutlinePixelsRetriever> {

    public abstract void traverse(
            Point3i root, List<Point3i> listOut, RandomNumberGenerator randomNumberGenerator)
            throws TraverseOutlineException;
}
