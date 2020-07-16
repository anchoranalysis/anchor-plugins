/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;

/**
 * A subset of a cfg (as extracted during a partition)
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class CfgFromPartition {

    @Getter private final Cfg cfg;

    @Getter private final PartitionedCfg partition;

    /** Makes a copy, with a different cfg */
    public CfgFromPartition copyChange(Cfg cfg) {
        return new CfgFromPartition(cfg, partition);
    }
}
