package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;

/**
 * A cfg extracted from a Partition of Cfgs
 * 
 * @author FEEHANO
 *
 */
public class CfgFromPartition {

	private Cfg cfg;
	
	private PartitionMarks<Mark> partition;
	
	public CfgFromPartition(Cfg cfg, PartitionMarks<Mark> partition) {
		super();
		assert(cfg!=null);
		this.cfg = cfg;
		this.partition = partition;
	}

	public Cfg getCfg() {
		return cfg;
	}
	
	/** Makes a copy, with a diffent cfg */
	public CfgFromPartition copyChange( Cfg cfg ) {
		return new CfgFromPartition( cfg, partition );
	}

	public PartitionMarks<Mark> getPartition() {
		return partition;
	}
}
