package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-ij
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


import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class ChnlProviderMedianFilterIJ2D extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private int radius = 2;
	
	@BeanField
	private boolean radiusInMeters = false;	// Treats radius if it's microns
	// END BEAN PROPERTIES
	
	public static Chnl median3d( Chnl chnl, int radius ) throws CreateException {
		

		//ImagePlus imp = ImgStackUtilities.createImagePlus(chnl);
		
		RankFilters rankFilters = new RankFilters();
		//rankFilters.setup("despeckle",null);
		
		VoxelBoxWrapper vb = chnl.getVoxelBox();
		
		// Are we missing a Z slice?
		for (int z=0; z<chnl.getDimensions().getZ(); z++) {
		
			ImageProcessor processor = IJWrap.imageProcessor(vb, z);
			rankFilters.rank( processor, radius, RankFilters.MEDIAN );
		}
		
		return chnl;
	}
	
	@Override
	public Chnl createFromChnl(Chnl chnl) throws CreateException {
		
		int radiusToUse = radius;
		
		if (radiusInMeters) {
			// Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care of later)
			radiusToUse = (int) Math.round(
				ImageUnitConverter.convertFromMeters( radius, chnl.getDimensions().getRes() )
			);
		}
		
		return median3d(chnl,radiusToUse);
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public boolean isRadiusInMeters() {
		return radiusInMeters;
	}

	public void setRadiusInMeters(boolean radiusInMeters) {
		this.radiusInMeters = radiusInMeters;
	}


}
