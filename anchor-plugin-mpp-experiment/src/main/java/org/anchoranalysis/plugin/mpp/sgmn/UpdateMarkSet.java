package org.anchoranalysis.plugin.mpp.sgmn;

import org.anchoranalysis.anchor.mpp.mark.Mark;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

import ch.ethz.biol.cell.beaninitparams.MPPInitParams;
import ch.ethz.biol.cell.mpp.pair.IUpdatableMarkSet;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.Pair;
import ch.ethz.biol.cell.mpp.pair.PairCollection;
import ch.ethz.biol.cell.mpp.pair.PxlMarkMemoList;
import ch.ethz.biol.cell.mpp.probmap.ProbMap;

class UpdateMarkSet {
	
	private MPPInitParams psoImage;
	private NRGStackWithParams nrgStack;
	private ListUpdatableMarkSetCollection updatableMarkSetCollection;
	private LogErrorReporter logger;
	
	public UpdateMarkSet(MPPInitParams psoImage, NRGStackWithParams nrgStack,
			ListUpdatableMarkSetCollection updatableMarkSetCollection, LogErrorReporter logger) {
		super();
		this.psoImage = psoImage;
		this.nrgStack = nrgStack;
		this.updatableMarkSetCollection = updatableMarkSetCollection;
		this.logger = logger;
	}
	
	public void apply() throws OperationFailedException {
		makePairsUpdatable();
		makeProbMapsUpdatable();
	}
	
	private void makePairsUpdatable() throws OperationFailedException {
		
		try {
			for( String key : psoImage.getSimplePairCollection().keys()  ) {
				PairCollection<Pair<Mark>> pair = psoImage.getSimplePairCollection().getException(key);
				pair.initUpdatableMarkSet(new PxlMarkMemoList(), nrgStack, logger, psoImage.getFeature().getSharedFeatureSet() );
				updatableMarkSetCollection.add(pair);
			}
		} catch (GetOperationFailedException | InitException e) {
			throw new OperationFailedException(e);
		}
	}
			
	private void makeProbMapsUpdatable() throws OperationFailedException {
		
		try {
			for( String key : psoImage.getProbMapSet().keys()  ) {
				ProbMap probMap = psoImage.getProbMapSet().getException(key);
				
				IUpdatableMarkSet updater = probMap.updater(); 
				if (updater!=null) { 
					updater.initUpdatableMarkSet( new PxlMarkMemoList(), nrgStack, logger, psoImage.getFeature().getSharedFeatureSet() );
					updatableMarkSetCollection.add( updater );
				}
			}
		} catch (GetOperationFailedException | InitException e) {
			throw new OperationFailedException(e);
		}
	}	
}
