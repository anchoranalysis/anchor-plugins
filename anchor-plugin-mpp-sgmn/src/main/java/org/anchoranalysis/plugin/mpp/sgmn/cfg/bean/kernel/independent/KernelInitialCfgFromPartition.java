package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.mark.extractweight.ConstantWeight;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.mark.extractweight.ExtractWeightFromMark;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.PartitionedCfg;

import lombok.Getter;
import lombok.Setter;

public class KernelInitialCfgFromPartition extends KernelIndependent<CfgFromPartition> {
	
	// START BEAN LIST
	@BeanField @Getter @Setter
	private CfgProposer cfgProposer;
	
	@BeanField @Getter @Setter
	private ExtractWeightFromMark extractWeight = new ConstantWeight();
	// END BEAN LIST
	
	private Cfg lastCfg;

	@Override
	public Optional<CfgFromPartition> makeProposal(Optional<CfgFromPartition> exst, KernelCalcContext context ) throws KernelCalcNRGException {
		
		Optional<Cfg> cfg = InitCfgUtilities.propose(cfgProposer, context);
		
		if (!cfg.isPresent()) {
			return Optional.empty();
		}
		
		this.lastCfg = cfg.get();
		
		return Optional.of(
			new CfgFromPartition(
				new Cfg(),
				createPartition(cfg.get())
			)
		);
	}

	@Override
	public double calcAccptProb(
		int exstSize,
		int propSize,
		double poissonIntensity,
		ImageDimensions dimensions,
		double densityRatio
	) {
		// We always accept
		return 1;
	}

	@Override
	public String dscrLast() {
		return String.format("initialCfgWithPartition(size=%d)", this.lastCfg.size());
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgFromPartition exst, CfgFromPartition accptd) throws UpdateMarkSetException {
		// NOTHING TO DO
	}

	@Override
	public int[] changedMarkIDArray() {
		return this.lastCfg.createIdArr();
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return cfgProposer.isCompatibleWith(testMark);
	}
	
	private PartitionedCfg createPartition( Cfg cfg ) {
		return new PartitionedCfg(
			cfg,
			mark-> extractWeight.weightFor(mark)
		);
	}
}
