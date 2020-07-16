/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;

/**
 * 1. Iterates over each object 2. For each object, with a random probability of 0.5, includes all
 * pixels inside the object
 *
 * @author feehano
 */
public class IncludeRandomObjects extends PointsProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public Optional<List<Point3i>> propose(
            Point3d point,
            Mark mark,
            ImageDimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode) {

        List<Point3i> out = new ArrayList<>();

        try {
            ObjectCollection objectCollection = objects.create();

            for (ObjectMask object : objectCollection) {
                maybeAddToList(object, out, randomNumberGenerator);
            }

        } catch (CreateException e) {
            errorNode.add(e);
            return Optional.empty();
        }

        return Optional.of(out);
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }

    private static void maybeAddToList(
            ObjectMask object, List<Point3i> out, RandomNumberGenerator randomNumberGenerator) {
        if (randomNumberGenerator.sampleDoubleZeroAndOne() > 0.5) {
            out.addAll(PointsFromObject.fromAsInteger(object));
        }
    }
}
