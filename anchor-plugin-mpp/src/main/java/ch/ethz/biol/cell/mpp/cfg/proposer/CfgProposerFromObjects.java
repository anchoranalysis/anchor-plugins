/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.proposer;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.EllipsoidFactory;

public class CfgProposerFromObjects extends CfgProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    @BeanField @Getter @Setter private boolean suppressZCovariance = false;

    @BeanField @Getter @Setter private double shellRad = 0.2;

    @BeanField @OptionalBean @Getter @Setter private CheckMark checkMark;
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkEllipsoid;
    }

    @Override
    public Optional<Cfg> propose(CfgGen cfgGen, ProposerContext context)
            throws ProposalAbnormalFailureException {

        ObjectCollection objectCollection;
        try {
            objectCollection = objects.create();
        } catch (CreateException e) {
            throw new ProposalAbnormalFailureException("Failed to create objects", e);
        }

        Cfg cfg = new Cfg();

        try {
            if (checkMark != null) {
                checkMark.start(context.getNrgStack());
            }
        } catch (OperationFailedException e) {
            throw new ProposalAbnormalFailureException("Failed to start checkMark", e);
        }

        try {
            for (ObjectMask object : objectCollection) {
                Mark mark = createFromObject(object, context.getNrgStack());
                mark.setId(cfgGen.idAndIncrement());

                if (checkMark == null
                        || checkMark.check(mark, context.getRegionMap(), context.getNrgStack())) {
                    cfg.add(mark);
                }
            }

            if (checkMark != null) {
                checkMark.end();
            }

            return Optional.of(cfg);

        } catch (CreateException e) {
            throw new ProposalAbnormalFailureException(
                    "Failed to create a mark from the obj-mask", e);
        } catch (CheckException e) {
            throw new ProposalAbnormalFailureException(
                    "A failure occurred while checking the mark", e);
        }
    }

    private MarkEllipsoid createFromObject(ObjectMask object, NRGStackWithParams nrgStack)
            throws CreateException {
        return EllipsoidFactory.createMarkEllipsoidLeastSquares(
                object, nrgStack.getDimensions(), suppressZCovariance, shellRad);
    }
}
