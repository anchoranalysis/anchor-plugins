/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.bound.Bound;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.mark.conic.RadiiRandomizer;
import org.anchoranalysis.anchor.mpp.mark.conic.bounds.RotationBounds3D;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.orientation.Orientation;

public class EllipsoidOrientationRadii extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField private RotationBounds3D rotationBounds = new RotationBounds3D();

    @BeanField private Bound radiusBound;
    // END BEAN PROPERTIES

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context) {

        MarkEllipsoid mark = (MarkEllipsoid) inputMark.getMark();
        inputMark.reset();

        Orientation orientation =
                rotationBounds.randomOrientation(
                        context.getRandomNumberGenerator(), context.getDimensions().getRes());

        Point3d radii =
                RadiiRandomizer.randomizeRadii(
                        radiusBound,
                        context.getRandomNumberGenerator(),
                        context.getDimensions().getRes(),
                        true);

        mark.setMarksExplicit(mark.getPos(), orientation, radii);

        return true;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipsoid;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }

    public RotationBounds3D getRotationBounds() {
        return rotationBounds;
    }

    public void setRotationBounds(RotationBounds3D rotationBounds) {
        this.rotationBounds = rotationBounds;
    }

    public Bound getRadiusBound() {
        return radiusBound;
    }

    public void setRadiusBound(Bound radiusBound) {
        this.radiusBound = radiusBound;
    }
}
