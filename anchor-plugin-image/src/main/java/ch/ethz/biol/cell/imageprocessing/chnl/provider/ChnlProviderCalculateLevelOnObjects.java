package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.threshold.calculatelevel.CalculateLevel;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResult;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollection;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollectionFactory;

// Calculates a threshold-level for each object independently
public class ChnlProviderCalculateLevelOnObjects extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN
	@BeanField @Optional
	private ObjMaskProvider objMaskProvider;
	
	@BeanField
	private ChnlProvider chnlProviderOutput;
	
	@BeanField
	private ChnlProvider chnlProviderIntensity;
	
	@BeanField
	private CalculateLevel calculateLevel;
	
	@BeanField
	private int numDilations = 0;
	// END BEAN
	
	static void setSeperatelyForEachObject( Chnl chnlIntensity, Chnl chnlOutput, ObjMaskCollection objMasks, CalculateLevel calculateLevel, int numDilations, LogErrorReporter logErrorReporter ) throws OperationFailedException {
		
		try {
			LevelResultCollection lrc = LevelResultCollectionFactory.createCollection( chnlIntensity, objMasks, calculateLevel, numDilations, logErrorReporter );
			VoxelBox<?> vbOutput = chnlOutput.getVoxelBox().any(); 
			for( LevelResult lr : lrc ) {
				vbOutput.setPixelsCheckMask(lr.getObjMask(), lr.getLevel());
			}
			
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	static void setGlobally( Chnl chnlIntensity, Chnl chnlOutput, CalculateLevel calculateLevel ) throws OperationFailedException {
		
		try {
			int val = calculateLevel.calculateLevel( HistogramFactoryUtilities.create(chnlIntensity) );
			
			VoxelBox<?> vbOutput = chnlOutput.getVoxelBox().any(); 
			vbOutput.setAllPixelsTo(val);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}

	}
	
	
	@Override
	public Chnl create() throws CreateException {

		try {
			Chnl chnlIntensity = chnlProviderIntensity.create();
			
			Chnl chnlOutput = chnlProviderOutput.create();
			
			if (objMaskProvider!=null) {
				ObjMaskCollection objMasks = objMaskProvider.create();
	
				setSeperatelyForEachObject(
					chnlIntensity,
					chnlOutput,
					objMasks,
					calculateLevel,
					numDilations,
					getLogger()
				);
				
			} else {
				setGlobally(
					chnlIntensity,
					chnlOutput,
					calculateLevel
				);
			}
			
			return chnlOutput;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
		
	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}


	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}

	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}


	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}


	public ChnlProvider getChnlProviderOutput() {
		return chnlProviderOutput;
	}


	public void setChnlProviderOutput(ChnlProvider chnlProviderOutput) {
		this.chnlProviderOutput = chnlProviderOutput;
	}


	public ChnlProvider getChnlProviderIntensity() {
		return chnlProviderIntensity;
	}


	public void setChnlProviderIntensity(ChnlProvider chnlProviderIntensity) {
		this.chnlProviderIntensity = chnlProviderIntensity;
	}


	public int getNumDilations() {
		return numDilations;
	}


	public void setNumDilations(int numDilations) {
		this.numDilations = numDilations;
	}
}
