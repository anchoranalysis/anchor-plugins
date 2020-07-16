/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.all;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoCollection;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CfgFromNrgHelper {

    public static Cfg cfgFromNrg(MemoCollection list) {
        Cfg cfg = new Cfg();
        for (int i = 0; i < list.size(); i++) {
            VoxelizedMarkMemo pmm = list.getMemoForIndex(i);
            cfg.add(pmm.getMark());
        }
        return cfg;
    }
}
