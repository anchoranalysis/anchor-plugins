package ch.ethz.biol.cell.mpp.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
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
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;

public class CentrePosOnBinaryImgChnl extends CheckMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1700747133659495372L;

	// START BEAN
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField
	private boolean acceptOutsideScene = false;
	
	@BeanField
	private boolean suppressZ = false;
	// END BEAN
	
	private BinaryChnl bi;
	
	@Override
	public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) throws CheckException {

		Point3d cp = mark.centerPoint();
		
		if (!nrgStack.getDimensions().contains(cp)) {
			return acceptOutsideScene;
		}
		
		try {
			bi = binaryImgChnlProvider.create();
		} catch (CreateException e) {
			throw new CheckException(
				String.format("Cannot create binaryImg (from provider %s)", binaryImgChnlProvider),
				e
			);
		}
			
		
		Point3i cpInt = PointConverter.intFromDouble(cp);
		
		if (suppressZ) {
			cpInt.setZ(0);
		}

		return bi.isPointOn(cpInt);
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

	public boolean isSuppressZ() {
		return suppressZ;
	}

	public void setSuppressZ(boolean suppressZ) {
		this.suppressZ = suppressZ;
	}

	@Override
	public FeatureList orderedListOfFeatures() {
		return null;
	}
}
