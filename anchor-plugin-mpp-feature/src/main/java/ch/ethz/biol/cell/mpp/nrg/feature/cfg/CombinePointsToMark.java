package ch.ethz.biol.cell.mpp.nrg.feature.cfg;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkPointList;

/*-
 * #%L
 * anchor-plugin-mpp-feature
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.Constant;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

import ch.ethz.biol.cell.imageprocessing.pixellist.factory.PixelPartFactoryHistogram;
import ch.ethz.biol.cell.mpp.mark.points.CreateMarkFromPoints;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;
import ch.ethz.biol.cell.mpp.mark.regionmap.RegionMap;
import ch.ethz.biol.cell.mpp.nrg.NRGElemAll;
import ch.ethz.biol.cell.mpp.nrg.NRGElemAllCalcParams;
import ch.ethz.biol.cell.mpp.nrg.NRGElemIndCalcParams;

/**
 * Assumes all marks are {@link MarkPointList} and combines the points from these marks
 * Then fits a mark from a PointersFitter
 * And calculates a feature on the fitted shape
 * 
 * @author FEEHANO
 *
 */
public class CombinePointsToMark extends NRGElemAll {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private CreateMarkFromPoints createMark;
	
	@BeanField
	private Feature item;
	
	@BeanField
	private Feature featureElse = new Constant(0);
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast(NRGElemAllCalcParams params) throws FeatureCalcException {

		try {
			Mark mark = createMark.fitMarkToPointsFromCfg(
				params.getPxlPartMemo().asCfg(),
				params.getDimensions()
			);
			
			if (mark!=null) {
				return calcFeatureOnMarks( mark, params.getNrgStack(), params.getPxlPartMemo().getRegionMap() );
			} else {
				return featureElse.calcCheckInit( params );
			}
			
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
		
	private double calcFeatureOnMarks( Mark mark, NRGStackWithParams nrgStack, RegionMap regionMap ) throws FeatureCalcException {
		
		PxlMarkMemo memo = new PxlMarkMemo(
			mark,
			nrgStack.getNrgStack(),
			regionMap,
			new PixelPartFactoryHistogram()
		);
							
		NRGElemIndCalcParams paramsMark = new NRGElemIndCalcParams(	memo, nrgStack );
		
		return item.calcCheckInit(paramsMark);
	}
	
	public Feature getFeatureElse() {
		return featureElse;
	}

	public void setFeatureElse(Feature featureElse) {
		this.featureElse = featureElse;
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

	public CreateMarkFromPoints getCreateMark() {
		return createMark;
	}

	public void setCreateMark(CreateMarkFromPoints createMark) {
		this.createMark = createMark;
	}
}
