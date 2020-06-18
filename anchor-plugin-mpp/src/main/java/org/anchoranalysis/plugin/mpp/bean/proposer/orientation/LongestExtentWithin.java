package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.Orientation2D;
import org.anchoranalysis.image.orientation.Orientation3DEulerAngles;

// Gets the longest extent within a certain ratio between the bounds,
//   and below the upper maximum
public class LongestExtentWithin extends OrientationProposer {

	// START BEAN
	@BeanField
	private double incrementDegrees = 1;
	
	@BeanField
	private double boundsRatio = 1.1;
	
	@BeanField
	private BoundCalculator boundCalculator;

	@BeanField
	private boolean rotateOnlyIn2DPlane = false;
	// END BEAN
	
	@Override
	public Optional<Orientation> propose(Mark mark, ImageDim dim, RandomNumberGenerator re ) throws ProposalAbnormalFailureException {
		
		try {
			RslvdBound minMaxBound = getSharedObjects().getMarkBounds().calcMinMax(dim.getRes(), dim.getZ()>1 );
			
			OrientationList listOrientations = findAllOrientations(mark, minMaxBound, dim);
			return Optional.of(
				listOrientations.sample(re)
			);
			
		} catch (NamedProviderGetException e) {
			throw new ProposalAbnormalFailureException(e.summarize());
		}
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractPosition;
	}
	
	private OrientationList findAllOrientations2D(Mark mark, RslvdBound minMaxBound, ImageDim dim) throws ProposalAbnormalFailureException {
		
		double incrementRadians = (incrementDegrees / 180) * Math.PI;
		
		OrientationList listOrientations = new OrientationList(boundCalculator, boundsRatio);
		
		// We loop through every positive angle and pick the one with the greatest extent
		for (double angle=0; angle < Math.PI; angle += incrementRadians) {
		
			Orientation orientation = new Orientation2D(angle);
			
			listOrientations.addOrientationIfUseful(orientation, mark, minMaxBound, dim);

		}
		
		return listOrientations;
	}
	
	
	private OrientationList findAllOrientations3D(Mark mark, RslvdBound minMaxBound, ImageDim dim) throws ProposalAbnormalFailureException {
		
		double incrementRadians = (incrementDegrees / 180) * Math.PI;
		
		OrientationList listOrientations = new OrientationList(boundCalculator, boundsRatio);
		
		// We loop through every positive angle and pick the one with the greatest extent
		for (double x=0; x < Math.PI; x += incrementRadians) {
			for (double y=0; y < Math.PI; y += incrementRadians) {
				for (double z=0; z < Math.PI; z += incrementRadians) {
					
					listOrientations.addOrientationIfUseful(
						new Orientation3DEulerAngles(x,y,z),
						mark,
						minMaxBound,
						dim
					);
				}
			}
		}
		
		return listOrientations;
	}
	
	
	private OrientationList findAllOrientations(Mark mark, RslvdBound minMaxBound, ImageDim dim) throws ProposalAbnormalFailureException {
		
		if (dim.getZ()>1 && !rotateOnlyIn2DPlane) {
			return findAllOrientations3D(mark, minMaxBound, dim);
		} else {
			return findAllOrientations2D(mark, minMaxBound, dim);
		}
	}
	
	public double getIncrementDegrees() {
		return incrementDegrees;
	}

	public void setIncrementDegrees(double incrementDegrees) {
		this.incrementDegrees = incrementDegrees;
	}

	public BoundCalculator getBoundCalculator() {
		return boundCalculator;
	}

	public void setBoundCalculator(BoundCalculator boundCalculator) {
		this.boundCalculator = boundCalculator;
	}

	public double getBoundsRatio() {
		return boundsRatio;
	}

	public void setBoundsRatio(double boundsRatio) {
		this.boundsRatio = boundsRatio;
	}

	public boolean isRotateOnlyIn2DPlane() {
		return rotateOnlyIn2DPlane;
	}

	public void setRotateOnlyIn2DPlane(boolean rotateOnlyIn2DPlane) {
		this.rotateOnlyIn2DPlane = rotateOnlyIn2DPlane;
	}
}
