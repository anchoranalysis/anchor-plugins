package ch.ethz.biol.cell.mpp.nrg.feature.session;

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;

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

import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation3DEulerAngles;

public class MarkFixture {

	private ImageDim dim;
	
	public MarkFixture( ImageDim dim ) {
		this.dim = dim;
	}
	
	// Intersects with Ellipsoid2
	public MarkEllipsoid createEllipsoid1() {
		
		MarkEllipsoid ell = new MarkEllipsoid();
		ell.setMarksExplicit(
			new Point3d(17,29,8),
			new Orientation3DEulerAngles(2.1,1.2, 2.0),
			new Point3d(5,11,3)
		);
		
		assert nonZeroBBoxVolume(ell);
		return ell;
	}
	
	// Intersects with Ellipsoid1
	public MarkEllipsoid createEllipsoid2() {
		
		MarkEllipsoid ell = new MarkEllipsoid();
		ell.setMarksExplicit(
			new Point3d(20,32,9),
			new Orientation3DEulerAngles(2.1,1.2, 2.0),
			new Point3d(5,11,6)
		);
		
		assert nonZeroBBoxVolume(ell);
		return ell;
	}
		
	public MarkEllipsoid createEllipsoid3() {
		
		MarkEllipsoid ell = new MarkEllipsoid();
		ell.setMarksExplicit(
			new Point3d(104,1,19),
			new Orientation3DEulerAngles(0.1,0.2, 0.5),
			new Point3d(12,21,7)
		);
		
		assert nonZeroBBoxVolume(ell);
		return ell;
	}
	
	private boolean nonZeroBBoxVolume(MarkEllipsoid ell) {
		return !ell.bbox(dim, GlobalRegionIdentifiers.SUBMARK_INSIDE).extnt().isEmpty();
	}
}
