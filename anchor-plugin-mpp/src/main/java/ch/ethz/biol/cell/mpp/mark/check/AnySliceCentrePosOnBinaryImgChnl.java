package ch.ethz.biol.cell.mpp.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.mark.Mark;

/*-
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.extent.BoundingBox;

import ch.ethz.biol.cell.core.CheckMark;

// The centre ps of at least one slice should be on the stack
//  Tries overall centre point first, and then projects it onto each slice
public class AnySliceCentrePosOnBinaryImgChnl extends CheckMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1700747133659495372L;

	// START BEAN
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField
	private boolean acceptOutsideScene = false;
	// END BEAN
	
	private BinaryChnl bi;
	
	private boolean isPointOnBinaryChnl( Point3d cp, NRGStackWithParams nrgStack ) throws CheckException {

		if (!nrgStack.getDimensions().contains(cp)) {
			return acceptOutsideScene;
		}
		
		try {
			bi = binaryImgChnlProvider.create();
		} catch (CreateException e) {
			throw new CheckException(
				String.format("Cannot create binary image (from provider %s)", binaryImgChnlProvider),
				e 
			);
		}
				
		Point3i cpInt = new Point3i((int) cp.getX(), (int) cp.getY(), (int) cp.getZ());

		return bi.isPointOn(cpInt);

	}
	
	@Override
	public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) throws CheckException {
		Point3d cp = mark.centerPoint();
		if(isPointOnBinaryChnl( cp, nrgStack )) {
			return true;
		}
		
		BoundingBox bbox = mark.bboxAllRegions( nrgStack.getDimensions() ) ;
		Point3i crnrMax = bbox.calcCrnrMax();
		for( int z=bbox.getCrnrMin().getZ(); z<=crnrMax.getZ(); z++) {
			Point3d cpSlice = new Point3d(cp.getX(), cp.getY(),z);
			if(isPointOnBinaryChnl( cpSlice, nrgStack )) {
				return true;
			}
		}
	
		return false;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	public boolean isAcceptOutsideScene() {
		return acceptOutsideScene;
	}

	public void setAcceptOutsideScene(boolean acceptOutsideScene) {
		this.acceptOutsideScene = acceptOutsideScene;
	}

	@Override
	public FeatureList orderedListOfFeatures() {
		return null;
	}

	
	

}
