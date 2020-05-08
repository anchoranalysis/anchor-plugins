package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;

/*
 * #%L
 * anchor-plugin-mpp-feature
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
import org.anchoranalysis.bean.shared.relation.EqualToBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark.PixelStatisticsFromMark;

// Number of unique pixel values 
public class NumberUniquePixelValues extends FeatureSingleMemo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN
	@BeanField
	private PixelStatisticsFromMark pixelList;
	// END BEAN
	
	@Override
	public double calc( SessionInput<FeatureInputSingleMemo> input ) throws FeatureCalcException {

		try {
			VoxelStatistics stats = pixelList.createStatisticsFor(
				input.get().getPxlPartMemo(),
				input.get().getDimensionsRequired()
			);
			
			int numUniqueValues = 0;
				
			for( int v=0; v<255; v++) {
				long cnt = stats.countThreshold(
					new RelationToConstant(
						new EqualToBean(),
						v
					)
				);
				
				if (cnt!=0) {
					numUniqueValues++;
				}
			}
			
			return numUniqueValues;
			
		} catch (IndexOutOfBoundsException e) {
			throw new FeatureCalcException(e);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	@Override
	public String getParamDscr() {
		return pixelList.getBeanDscr();
	}
	
	public PixelStatisticsFromMark getPixelList() {
		return pixelList;
	}


	public void setPixelList(PixelStatisticsFromMark pixelList) {
		this.pixelList = pixelList;
	}

}
