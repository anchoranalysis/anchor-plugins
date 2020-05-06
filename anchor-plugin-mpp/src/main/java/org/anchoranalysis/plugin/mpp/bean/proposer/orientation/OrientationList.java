package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;

class OrientationList {

	private BoundCalculator boundCalculator;
	private double boundsRatio;
	
	private List<Orientation> listOrientationsWithinBoundsRatio = new ArrayList<>();
	private List<Orientation> listOrientationsUnbounded = new ArrayList<>();
			
	public OrientationList(BoundCalculator boundCalculator, double boundsRatio) {
		super();
		this.boundCalculator = boundCalculator;
		this.boundsRatio = boundsRatio;
	}		
	
	public void addOrientationIfUseful( Orientation orientation, Mark mark, RslvdBound minMaxBound, ImageDim dim, ErrorNode errorNode ) {
		
		BidirectionalBound bib = boundCalculator.calcBound( mark.centerPoint(), orientation.createRotationMatrix(), errorNode);
		
		if (bib==null) {
			errorNode.add("null bound");
			return;
		}
		
		if (bib.isUnboundedAtBothEnds()) {
			errorNode.add("unbounded at both ends");
			return;
		}
		
		if (bib.isUnbounded()) {
			
			double max = bib.getMaxOfMax();
			if (max < minMaxBound.getMax()) {
				listOrientationsUnbounded.add(orientation);
			} else {
				errorNode.addFormatted("unbounded is above marks bound max (%f > %f)", max, minMaxBound.getMax());
			}
		}
		
		double rb = bib.ratioBounds(dim); 
		
		if (rb > boundsRatio) {
			errorNode.addFormatted("outside bounds ratio (%f br=%f)",rb, boundsRatio);
			return;
		}
		
		double max = bib.getMaxOfMax();
		
		if (max > minMaxBound.getMax()) {
			errorNode.addFormatted("above marks bound max (%f > %f)",max, minMaxBound.getMax());
			return;
		}
		
		listOrientationsWithinBoundsRatio.add(orientation);
		
		// We deliberately don't consider the max, if it is outside our mark
		// We record it as a possible value, and we pick from the arraylist at the end
	}

	
	// We adopt the following priority
	//		If there are orientations within the Bounds Ratio, WE SAMPLE UNIFORMLY FROM THEM
	//		If not, and there are unbounded orientations, WE SAMPLE UNIFORMLY FROM THEM
	public Orientation sample( RandomNumberGenerator re ) {

		if (listOrientationsWithinBoundsRatio.size()>0) {
			return listOrientationsWithinBoundsRatio.get( (int) (re.nextDouble() * listOrientationsWithinBoundsRatio.size()) );
		} else if (listOrientationsUnbounded.size()>0) {
			return listOrientationsUnbounded.get( (int) (re.nextDouble() * listOrientationsUnbounded.size()) );
		} else {
			return null;
		}
	}
}