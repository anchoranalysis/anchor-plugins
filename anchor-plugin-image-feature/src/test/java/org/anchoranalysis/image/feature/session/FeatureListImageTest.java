package org.anchoranalysis.image.feature.session;

import static org.anchoranalysis.test.feature.plugins.ResultsVectorTestUtilities.*;

/*-
 * #%L
 * anchor-test-feature-plugins
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

import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.SessionFactory;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramParams;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.ConstantsInListFixture;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;
import org.anchoranalysis.test.feature.plugins.HistogramFixture;
import org.anchoranalysis.test.feature.plugins.NRGStackFixture;
import org.junit.Before;
import org.junit.Test;

/**
 * This test is located inm this package, as it uses BeanXML in resources that depends on plugins in this module
 * @author owen
 *
 */
public class FeatureListImageTest {
	
	private static TestLoader loader = TestLoader.createFromMavenWorkingDir();
	
	private static NRGStackWithParams NRG_STACK = NRGStackFixture.create(true, true);
	
	@Before
    public void setUp() {
		RegisterBeanFactories.registerAllPackageBeanFactories();
    }
	
	@Test(expected = FeatureCalcException.class)
	public void testNoParams() throws InitException, FeatureCalcException, CreateException {
		
		FeatureCalculatorMulti<FeatureCalcParams> session = createAndStart(ConstantsInListFixture.create());
		
		ResultsVector rv1 = session.calcOne( (FeatureCalcParams) null );
		ConstantsInListFixture.checkResultVector(rv1);
		
		ResultsVector rv2 = session.calcOne( (FeatureCalcParams) null );
		ConstantsInListFixture.checkResultVector(rv2);
	}
	
	
	@Test
	public void testHistogram() throws InitException, FeatureCalcException, CreateException {
		
		FeatureCalculatorMulti<FeatureHistogramParams> session = createAndStart(
			histogramFeatures(loader)
		);
		
		assertCalc(
			session.calcOne(
				createParams(HistogramFixture.createAscending())
			),
			32450.0, 30870.0, 14685.0, 140.0, 214.0, 0.005545343137254902					
		);
		
		assertCalc(
			session.calcOne(
				createParams(HistogramFixture.createDescending())
			),
			27730.0, 19110.0, 2145.0, 41.0, 115.0, 0.0022671568627450982					
		);
	}
		
	@Test
	public void testImage() throws InitException, FeatureCalcException, CreateException {
		
		FeatureCalculatorMulti<FeatureObjMaskParams> session = createAndStart(
			objMaskFeatures(loader)
		);
		
		ObjMaskFixture objMaskFixture = new ObjMaskFixture(
			NRG_STACK.getDimensions()
		);
		
		assertCalc(
			session.calcOne(
				createParams(objMaskFixture.create1())
			),
			31.5, 29.0, 3.0, 59.0, 225.02857142857144, 2744.0, 560.0					
		);
		
		assertCalc(
			session.calcOne(
				createParams(objMaskFixture.create2())
			),
			7.5, 21.0, 7.0, 28.5, 195.93956043956044, 108.0, 66.0					
		);
		
		assertCalc(
			session.calcOne(
				createParams(objMaskFixture.create3())
			),
			21.5, 35.0, 2.0, 55.5, 159.37306501547988, 612.0, 162.0					
		);
	}
	
	
	
	
	private <T extends FeatureCalcParams> FeatureCalculatorMulti<T> createAndStart( FeatureList<T> features ) throws FeatureCalcException {
		return SessionFactory.createAndStart(
			features,
			LoggingFixture.simpleLogErrorReporter()
		);
	}
	
	
	/** creates a feature-list associated with the fixture
	 *  
	 * @throws CreateException 
	 * */
	private static FeatureList<FeatureHistogramParams> histogramFeatures( TestLoader loader ) throws CreateException {
		return FeaturesFromXmlFixture.createFeatureList("histogramFeatureList.xml", loader);
	}
	
	/** creates a feature-list associated with obj-mask
	 *  
	 * @throws CreateException 
	 * */
	private static FeatureList<FeatureObjMaskParams> objMaskFeatures( TestLoader loader ) throws CreateException {
		return FeaturesFromXmlFixture.createFeatureList("objMaskFeatureList.xml", loader);
	}

	private static FeatureHistogramParams createParams( Histogram hist ) throws CreateException {
		return new FeatureHistogramParams(
			hist,
			NRG_STACK.getDimensions().getRes()
		);
	}
	
	private static FeatureObjMaskParams createParams( ObjMask om ) throws CreateException {
		FeatureObjMaskParams params = new FeatureObjMaskParams(om);
		params.setNrgStack(NRG_STACK);
		return params;
	}
}
