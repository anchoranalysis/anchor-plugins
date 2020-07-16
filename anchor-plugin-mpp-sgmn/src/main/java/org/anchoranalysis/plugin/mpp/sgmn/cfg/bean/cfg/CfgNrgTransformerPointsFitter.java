/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.points.CreateMarkFromPoints;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgNRGPixelizedFactory;

public class CfgNrgTransformerPointsFitter extends StateTransformerBean<Cfg, CfgNRGPixelized> {

    // START BEAN PROPERTIES
    @BeanField private CreateMarkFromPoints createMark;
    // END BEAN PROPERTIES

    @Override
    public CfgNRGPixelized transform(Cfg in, TransformationContext context)
            throws OperationFailedException {

        Optional<Mark> mark = createMark.fitMarkToPointsFromCfg(in, context.getDimensions());

        // If we cannot create a mark, there is no proposal
        Cfg cfg = wrapMark(mark);

        try {
            return CfgNRGPixelizedFactory.createFromCfg(
                    cfg, context.getKernelCalcContext(), context.getLogger());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    public CreateMarkFromPoints getCreateMark() {
        return createMark;
    }

    public void setCreateMark(CreateMarkFromPoints createMark) {
        this.createMark = createMark;
    }

    private static Cfg wrapMark(Optional<Mark> mark) {
        Cfg cfg = new Cfg();
        mark.ifPresent(cfg::add);
        return cfg;
    }
}
