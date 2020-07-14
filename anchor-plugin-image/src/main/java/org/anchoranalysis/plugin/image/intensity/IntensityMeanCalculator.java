package org.anchoranalysis.plugin.image.intensity;

/*-
 * #%L
 * anchor-plugin-image
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

import java.nio.ByteBuffer;

import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class IntensityMeanCalculator {
	
	public static double calcMeanIntensityObject( Channel chnl, ObjectMask object ) throws FeatureCalcException {
		return calcMeanIntensityObject(chnl, object, false);
	}
	
	public static double calcMeanIntensityObject( Channel chnl, ObjectMask object, boolean excludeZero ) throws FeatureCalcException {
		checkContained(object.getBoundingBox(), chnl.getDimensions().getExtent());
		
		VoxelBoxWrapper vbIntensity = chnl.getVoxelBox();
		
		BoundingBox bbox = object.getBoundingBox();
		
		ReadableTuple3i crnrMin = bbox.cornerMin();
		ReadableTuple3i crnrMax = bbox.calcCornerMax();
		
		double sum = 0.0;
		int cnt = 0;
		
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++) {
			
			VoxelBuffer<?> bbIntens = vbIntensity.any().getPixelsForPlane( z );
			ByteBuffer bbMask = object.getVoxelBox().getPixelsForPlane( z - crnrMin.getZ() ).buffer();
			
			int offsetMask = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
				
					if (bbMask.get(offsetMask)==object.getBinaryValuesByte().getOnByte()) {
						int offsetIntens = vbIntensity.any().extent().offset(x, y);
						
						int val = bbIntens.getInt(offsetIntens);
						
						if(excludeZero && val==0) {
							offsetMask++;
							continue;
						}
						
						sum += val;
						cnt++;
					}
							
					offsetMask++;
				}
			}
		}
		
		if (cnt==0) {
			throw new FeatureCalcException("There are 0 pixels in the object-mask");
		}
		
		return sum/cnt;
	}
	
	private static void checkContained(BoundingBox bbox, Extent extent) throws FeatureCalcException {
		if (!extent.contains(bbox)) {
			throw new FeatureCalcException(
				String.format(
					"The object's bounding-box (%s) is not contained within the dimensions of the channel %s",
					bbox,
					extent
				)
			);
		}
	}
}
