package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.FloatBuffer;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Calculates the gradient in one or more dimensions
 * 
 * @author Owen Feehan
 *
 */
class GradientCalculator {

	private boolean[] dimensions;
	private float scaleFactor;
	private int addSum;
	 
	/** calculate central-difference instead of backward-difference (finite differences) */
	private boolean centralDiff = false;
	

	/** iff TRUE, we apply a l2norm to our difference (useful for getting magnitude if working with more than 1 dimension)? */
	private boolean norm = true;
	
	/**
	 * Constructor
	 * 
	 * @param dimensions a boolean array indicating whether a dimension (X,Y,Z) should be included in the calculation
	 * @param scaleFactor the gradient is multiplied by this constant in the output
	 * @param addSum adds a constant after scale-factor has been applied (useful for shifting negative numbers into a positive range)
	 */
	public GradientCalculator(boolean[] dimensions, float scaleFactor, int addSum) {
		super();
		this.dimensions = dimensions;
		this.scaleFactor = scaleFactor;
		this.addSum = addSum;
	}


	/**
	 * Calculates the gradient of a channel
	 * 
	 * @param signalIn where to calculate gradient from
	 * @param gradientOut where to output the gradient to

	 * @throws CreateException
	 */
	public void calculateGradient(
		VoxelBoxWrapper signalIn,
		VoxelBox<FloatBuffer> gradientOut
	) throws CreateException {
		
		calcGradientImgLib2(
			ImgLib2Wrap.wrap(signalIn),				// Input channel
			ImgLib2Wrap.wrapFloat(gradientOut)		// Output channel
		);
	}

	public boolean isCentralDiff() {
		return centralDiff;
	}


	public void setCentralDiff(boolean centralDiff) {
		this.centralDiff = centralDiff;
	}


	public boolean isNorm() {
		return norm;
	}

	public void setNorm(boolean norm) {
		this.norm = norm;
	}
	

	/**
	 * Uses ImgLib2 to calculate gradient
	 * 
	 * <p>See <a href="https://github.com/imglib/imglib2-algorithm-gpl/blob/master/src/main/java/net/imglib2/algorithm/pde/Gradient.java>ImgLib2</a></p>
	 * 
	 * @param input input-image
	 * @param output output-image
	 */
	private void calcGradientImgLib2( Img<? extends RealType<?>> input, Img<FloatType> output ) {
		
		Cursor<? extends RealType<?>> in = Views.iterable(input).localizingCursor();
		RandomAccess<FloatType> oc = output.randomAccess();
		
		OutOfBounds<? extends RealType<?>> ra = Views.extendMirrorDouble(input).randomAccess(); 
		
 		while (in.hasNext()) {
			in.fwd();

			// Position neighborhood cursor;
			ra.setPosition( in );

			// Position output cursor
			for (int i = 0; i < input.numDimensions(); i++ ) {
				oc.setPosition( in.getLongPosition( i ), i );
			}
			
			double diffSum = processDimensions(
				ra,
				input.numDimensions(),
				in.get().getRealFloat()		// central value
			);
			
			oc.get().set(
				calculateOutputPixel(diffSum)
			);
		}

	}
	
	private double processDimensions(OutOfBounds<? extends RealType<?>> ra, int numDims, float central) {
		
		double diffSum = 0.0;
		
		for (int i=0; i<numDims; i++) {
			
			// Skip any dimension that isn't included
			if ( !dimensions[i] )	{
				continue;
			}
			
			ra.fwd(i);
			float diff = central - ra.get().getRealFloat();
			ra.bck(i);
			
			if (centralDiff) {
				ra.bck(i);
				diff += ra.get().getRealFloat();
				ra.fwd(i);
			}
							
			if (norm) {
				diffSum += Math.pow(diff,2.0);
			} else {
				diffSum += diff;
			}
		}
		return diffSum;
	}
	
	private float calculateOutputPixel( double diffSum ) {
		float diffOut = (float) maybeNorm(diffSum);
		return (diffOut*scaleFactor)+addSum;
	}
	
	private double maybeNorm( double diffSum ) {
		if (norm) {
			return Math.sqrt( diffSum );
		} else {
			return diffSum;
		}
	}
}