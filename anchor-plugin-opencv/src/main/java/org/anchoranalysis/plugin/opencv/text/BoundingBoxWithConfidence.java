package org.anchoranalysis.plugin.opencv.text;

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

import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.scale.ScaleFactor;

class BoundingBoxWithConfidence implements Comparable<BoundingBoxWithConfidence> {
	
	private BoundingBox bbox;
	private double confidence;
	
	public BoundingBoxWithConfidence(BoundingBox bbox, double confidence) {
		super();
		this.bbox = bbox;
		this.confidence = confidence;
	}

	public BoundingBox getBBox() {
		return bbox;
	}

	public double getConfidence() {
		return confidence;
	}
	
	
	/**
	 * The Intersection over Union (IoU) metric for two bounding-boxes.
	 * 
	 * @see <a href="https://www.quora.com/How-does-non-maximum-suppression-work-in-object-detection">Intersection-over-Union</a>
	 * @param othr the other bounding-box to consider when calculating the IoU
	 * @return the IoU score
	 */
	public double intersectionOverUnion(BoundingBox othr) {
		
		BoundingBox intersection = new BoundingBox(bbox);
		if (!intersection.intersect(othr, true)) {
			// If there's no intersection then the score is 0
			return 0.0;
		}
		
		int intersectionArea = intersection.extnt().getVolume();
		
		// The total area is equal to the sum of both minus the intersection (which is otherwise counted twice)
		int total = othr.extnt().getVolume() + bbox.extnt().getVolume() - intersectionArea;
		
		return ((double) intersectionArea) / total;
	}

	public void scaleXYPosAndExtnt( ScaleFactor sf ) {
		bbox.scaleXYPosAndExtnt( sf.getX(), sf.getY() );
	}

	@Override
	public int compareTo(BoundingBoxWithConfidence other) {
		return Double.compare(other.confidence, confidence);
	}
}
