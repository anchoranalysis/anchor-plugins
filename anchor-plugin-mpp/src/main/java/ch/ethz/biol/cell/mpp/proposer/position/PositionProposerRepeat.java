/* (C)2020 */
package ch.ethz.biol.cell.mpp.proposer.position;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.PositionProposerBean;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;

public class PositionProposerRepeat extends PositionProposerBean {

    // START BEAN PROPERTIES
    @BeanField private PositionProposerBean positionProposer;

    @BeanField private int maxIter;
    // END BEAN PROPERTIES

    @Override
    public Optional<Point3d> propose(ProposerContext context) {

        for (int i = 0; i < maxIter; i++) {
            Optional<Point3d> point =
                    positionProposer.propose(context.addErrorLevel(positionProposer.getBeanName()));

            if (point.isPresent()) {
                return point;
            }
        }

        return Optional.empty();
    }

    public PositionProposerBean getPositionProposer() {
        return positionProposer;
    }

    public void setPositionProposer(PositionProposerBean positionProposer) {
        this.positionProposer = positionProposer;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }
}
