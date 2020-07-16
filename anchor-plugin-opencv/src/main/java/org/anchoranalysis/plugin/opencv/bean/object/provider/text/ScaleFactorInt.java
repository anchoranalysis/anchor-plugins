package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.extent.Extent;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

class ScaleFactorInt {

	private int x;
	private int y;
	
	public ScaleFactorInt(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Point2i scale( Point2i point ) {
		return new Point2i(
			scaledX( point.getX() ),
			scaledY( point.getY() )
		);
	}

	public Extent scale( Extent extent ) {
		return new Extent(
			scaledX(extent.getX()),
			scaledY(extent.getY()),
			extent.getZ()
		);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	private int scaledX( int val ) {
		return val*x;
	}
	
	private int scaledY( int val ) {
		return val*y;
	}
}