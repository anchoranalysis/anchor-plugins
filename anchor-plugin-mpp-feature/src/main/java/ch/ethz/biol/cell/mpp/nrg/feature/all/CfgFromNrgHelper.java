package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoMarks;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

public class CfgFromNrgHelper {
	
	public static Cfg cfgFromNrg( MemoMarks list ) {
		Cfg cfg = new Cfg();
		for( int i=0; i<list.size(); i++ ) {
			PxlMarkMemo pmm = list.getMemoForIndex(i);
			cfg.add( pmm.getMark() );
		}
		return cfg;
	}
}
