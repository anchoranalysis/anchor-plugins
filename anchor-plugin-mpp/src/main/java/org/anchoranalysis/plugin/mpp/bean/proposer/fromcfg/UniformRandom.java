/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.fromcfg;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;

public class UniformRandom extends MarkFromCfgProposer {

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public Optional<Mark> markFromCfg(Cfg cfg, ProposerContext context) {
        // Let's take a mark at random
        int index = cfg.randomIndex(context.getRandomNumberGenerator());
        return Optional.of(cfg.get(index));
    }
}
