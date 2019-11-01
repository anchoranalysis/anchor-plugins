package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;

/*
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */



import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.bean.mark.extractweight.ConstantWeight;
import org.anchoranalysis.plugin.mpp.sgmn.bean.mark.extractweight.ExtractWeightFromMark;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.CfgFromPartition;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.PartitionMarks;

import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.cfg.proposer.CfgProposer;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;

public class KernelInitialCfgFromPartition extends KernelIndependent<CfgFromPartition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8499354370753415454L;
	
	// START BEAN LIST
	@BeanField
	private CfgProposer cfgProposer;
	
	@BeanField
	private ExtractWeightFromMark extractWeight = new ConstantWeight();
	// END BEAN LIST
	
	private Cfg lastCfg;
	
	public KernelInitialCfgFromPartition() {
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return cfgProposer.isCompatibleWith(testMark);
	}

	@Override
	public CfgFromPartition makeProposal(CfgFromPartition exst, KernelCalcContext context ) throws KernelCalcNRGException {
		
		Cfg cfg = InitCfgUtilities.propose(cfgProposer, context);
		
		if (cfg==null) {
			return null;
		}
		
		this.lastCfg = cfg;
		
		return new CfgFromPartition(
			new Cfg(),
			createPartition(cfg)
		);
	}

	@Override
	public double calcAccptProb(int exstSize, int propSize,
			double poisson_intens, ImageDim scene_size, double densityRatio) {
		// We always accept
		return 1;
	}

	@Override
	public String dscrLast() {
		return String.format("initialCfgWithPartition(size=%d)", this.lastCfg.size());
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgFromPartition exst, CfgFromPartition accptd) throws UpdateMarkSetException {

	}

	@Override
	public int[] changedMarkIDArray() {
		return this.lastCfg.createIdArr();
	}

	public CfgProposer getCfgProposer() {
		return cfgProposer;
	}

	public void setCfgProposer(CfgProposer cfgProposer) {
		this.cfgProposer = cfgProposer;
	}

	public ExtractWeightFromMark getExtractWeight() {
		return extractWeight;
	}

	public void setExtractWeight(ExtractWeightFromMark extractWeight) {
		this.extractWeight = extractWeight;
	}
		
	private PartitionMarks<Mark> createPartition( Cfg cfg ) {
		return new PartitionMarks<Mark>(
			cfg.getMarks(),
			mark-> extractWeight.weightFor(mark)
		);
	}
}
