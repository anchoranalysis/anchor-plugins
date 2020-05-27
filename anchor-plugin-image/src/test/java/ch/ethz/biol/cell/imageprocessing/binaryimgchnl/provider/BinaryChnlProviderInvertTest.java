package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import static org.junit.Assert.*;

import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.plugin.image.test.ProviderFixture;
import org.junit.Test;
import static ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlFixture.*;

public class BinaryChnlProviderInvertTest {
	
	private static final Point3i CORNER_RECTANGLE = new Point3i(7,14,0);
	private static final Point3i CORNER_MASK = addHalfHeightInY(CORNER_RECTANGLE);
	
	@Test
	public void testWithoutMask2d() throws CreateException {
		testRectangle(
			false,
			false,
			expectedNumPixelsAfterWithoutMask(false)
		);
	}
	
	@Test
	public void testWithoutMask3d() throws CreateException {
		testRectangle(
			true,
			false,
			expectedNumPixelsAfterWithoutMask(true)
		);
	}
	
	@Test
	public void testWithMask2d() throws CreateException {
		testRectangle(
			false,
			true,
			240
		);
	}
	
	@Test
	public void testWithMask3d() throws CreateException {
		testRectangle(
			true,
			true,
			720
		);
	}
	
	private static int expectedNumPixelsBefore(boolean do3D) {
		return WIDTH * HEIGHT * depth(do3D);
	}
	
	private static long expectedNumPixelsAfterWithoutMask(boolean do3D) {
		return extent(do3D).getVolume() - expectedNumPixelsBefore(do3D);
	}
	
	private static void testRectangle(boolean do3D, boolean mask, long expectedNumPixelsAfter) throws CreateException {
		
		BinaryChnl chnlBefore = createWithRectangle(CORNER_RECTANGLE, do3D);
		
		Optional<BinaryChnl> chnlMask = createMask(do3D, mask);
		
		assertPixelsOn(
			"before",
			expectedNumPixelsBefore(do3D),
			chnlBefore
		);
		
		BinaryChnl chnlAfter = createProviderInvert(chnlBefore, chnlMask).create(); 
		
		assertPixelsOn(
			"after",
			expectedNumPixelsAfter,
			chnlAfter
		);
	}
	
	private static Optional<BinaryChnl> createMask(boolean do3D, boolean mask) throws CreateException {
		if (mask) {
			return Optional.of(
				createWithRectangle(CORNER_MASK, do3D)		
			);
		} else {
			return Optional.empty(); 
		}
	}
	
	private static BinaryChnlProvider createProviderInvert(BinaryChnl chnl, Optional<BinaryChnl> mask) {
		BinaryChnlProviderInvert provider = new BinaryChnlProviderInvert();
		provider.setBinaryChnl(
			ProviderFixture.providerFor(chnl)
		);
		mask.ifPresent( m->
			provider.setMask(
				ProviderFixture.providerFor(m)
			)
		);
		return provider;
	}
	
	private static void assertPixelsOn(String messagePrefix, long expectedNumPixels, BinaryChnl chnl ) {
		assertEquals( messagePrefix + "PixelsOn", expectedNumPixels, chnl.binaryVoxelBox().countOn() );
	}
	
	private static Point3i addHalfHeightInY(Point3i in) {
		Point3i out = new Point3i(in);
		out.incrY( HEIGHT/2 );
		return out;
	}
}
