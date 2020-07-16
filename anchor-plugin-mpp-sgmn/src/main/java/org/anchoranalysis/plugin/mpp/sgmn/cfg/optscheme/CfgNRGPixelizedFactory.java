/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgWithNRGTotal;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CfgNRGPixelizedFactory {

    public static CfgNRGPixelized createFromCfg(Cfg cfg, KernelCalcContext context, Logger logger)
            throws CreateException {
        try {
            return new CfgNRGPixelized(
                    new CfgNRG(new CfgWithNRGTotal(cfg, context.getNrgScheme())),
                    context.proposer().getNrgStack(),
                    context.getNrgScheme().getSharedFeatures(),
                    logger);
        } catch (FeatureCalcException | InitException e) {
            throw new CreateException(e);
        }
    }
}
