package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor @EqualsAndHashCode(callSuper=false)
public abstract class CalculateOverlapMIPBase extends FeatureCalculation<Double, FeatureInputPairMemo> {

	private final int regionID;

	@Override
	protected Double execute( FeatureInputPairMemo params ) {
		
		VoxelizedMarkMemo mark1 = params.getObj1();
		VoxelizedMarkMemo mark2 = params.getObj2();
		
		assert( mark1 != null );
		assert( mark2 != null );
		
		VoxelizedMark pm1 = mark1.voxelized();
		VoxelizedMark pm2 = mark2.voxelized();
		
		if (!pm1.getBoundingBoxMIP().intersection().existsWith(pm2.getBoundingBoxMIP())) {
			return 0.0;
		}
		
		MaxIntensityProjectionPair pair =
			new MaxIntensityProjectionPair(
				pm1.getVoxelBoxMIP(),
				pm2.getVoxelBoxMIP(),
				regionMembershipForMark(mark1),
				regionMembershipForMark(mark2)
			);
		
		double overlap = pair.countIntersectingVoxels();
		
		return calculateOverlapResult(overlap, pair);
	}
	
	protected abstract Double calculateOverlapResult( double overlap, MaxIntensityProjectionPair pair);
		
	private RegionMembershipWithFlags regionMembershipForMark( VoxelizedMarkMemo mark ) {
		return mark.getRegionMap().membershipWithFlagsForIndex(regionID);
	}
}