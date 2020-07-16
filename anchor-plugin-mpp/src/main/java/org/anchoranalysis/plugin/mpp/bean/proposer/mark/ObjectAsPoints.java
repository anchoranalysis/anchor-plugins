/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointList;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;

public class ObjectAsPoints extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    private List<List<Point3d>> points = null;

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context)
            throws ProposalAbnormalFailureException {

        try {
            createObjectsIfNecessary();
        } catch (CreateException e) {
            context.getErrorNode().add(e);
            return false;
        }

        MarkPointList mark =
                MarkPointListFactory.create(
                        // Selects an object randomly
                        randomlySelectPoints(context));

        inputMark.assignFrom(mark);
        inputMark.reset();

        return true;
    }

    private List<Point3d> randomlySelectPoints(ProposerContext context) {
        return context.getRandomNumberGenerator().sampleFromList(points);
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }

    private void createObjectsIfNecessary() throws CreateException {
        if (points == null) {
            points = new ArrayList<>();

            for (ObjectMask object : objects.create()) {
                points.add(PointsFromObject.fromAsDouble(object));
            }
        }
    }
}
