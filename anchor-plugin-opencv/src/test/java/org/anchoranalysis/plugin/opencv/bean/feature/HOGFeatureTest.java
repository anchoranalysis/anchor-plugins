package org.anchoranalysis.plugin.opencv.bean.feature;

import static org.junit.Assert.*;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class HOGFeatureTest {

	private TestLoaderImageIO testLoader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	private NRGStack stack = new NRGStack(
		testLoader.openStackFromTestPath("car.jpg")
	);
	
	@Test
	public void testWithinBounds() throws FeatureCalcException {
		assertEquals(0.006448161, featureValForIndex(0), 10e-6);
	}
	
	@Test(expected = FeatureCalcException.class)
	public void testOutsideBounds() throws FeatureCalcException {
		featureValForIndex(60000);
	}
	
	private double featureValForIndex( int index ) throws FeatureCalcException {
				
		HOGFeature feature = new HOGFeature();
		feature.setResizeTo( new SizeXY(64,64) );
		feature.setIndex(index);
		
		FeatureCalculatorSingle<FeatureInputStack> session = FeatureSession.with(
			feature,
			LoggingFixture.simpleLogErrorReporter()
		);
		return session.calc(
			new FeatureInputStack(stack)
		);
	}
}
