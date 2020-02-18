package ch.ethz.biol.cell.mpp.nrg.feature.session;

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
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.log.ConsoleLogReporter;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.session.SimpleFeatureListFixture;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParams;
import org.junit.Before;
import org.junit.Test;

import anchor.test.TestLoader;

import static ch.ethz.biol.cell.mpp.nrg.feature.session.ResultsVectorTestUtilities.*;

public class FeatureSessionCreateParamsTest {
	
	private static TestLoader loader = TestLoader.createFromMavenWorkingDirMain();
	
	@Before
    public void setUp() {
		RegisterBeanFactories.registerAllPackageBeanFactories(false);
    }
	
	@Test
	public void testNoParams() throws InitException, FeatureCalcException, CreateException {
		
		FeatureSessionCreateParams session = createAndStart(SimpleFeatureListFixture.create(), false);
		
		ResultsVector rv1 = session.calc();
		SimpleFeatureListFixture.checkResultVector(rv1);
		
		ResultsVector rv2 = session.calc();
		SimpleFeatureListFixture.checkResultVector(rv2);
	}
	
	
	@Test
	public void testHistogram() throws InitException, FeatureCalcException, CreateException {
		
		FeatureSessionCreateParams session = createAndStart(
			FeatureListFixture.histogram(loader),
			true
		);
		
		assertCalc(
			session.calc( HistogramFixture.createAscending() ),
			32450.0, 30870.0, 14685.0, 140.0, 214.0, 0.005545343137254902					
		);
		
		assertCalc(
			session.calc( HistogramFixture.createDescending() ),
			27730.0, 19110.0, 2145.0, 41.0, 115.0, 0.0022671568627450982					
		);
	}
	
	@Test
	public void testImage() throws InitException, FeatureCalcException, CreateException {
		
		FeatureSessionCreateParams session = createAndStart(
			FeatureListFixture.objMask(loader),
			true
		);
		session.setNrgStack( NRGStackFixture.create() );
		
		ObjMaskFixture objMaskFixture = new ObjMaskFixture( session.getNrgStack().getDimensions() );
		
		assertCalc(
			session.calc( objMaskFixture.create1() ),
			31.5, 29.0, 3.0, 59.0, 225.02857142857144, 2744.0, 560.0					
		);
		
		assertCalc(
			session.calc( objMaskFixture.create2() ),
			7.5, 21.0, 7.0, 28.5, 195.93956043956044, 108.0, 66.0					
		);
		
		assertCalc(
			session.calc( objMaskFixture.create3() ),
			21.5, 35.0, 2.0, 55.5, 159.37306501547988, 612.0, 162.0					
		);
	}
	
	
	
	
	private FeatureSessionCreateParams createAndStart( FeatureList features, boolean withNrgStack ) throws InitException, CreateException {
		FeatureSessionCreateParams session = new FeatureSessionCreateParams(features);
		session.start( new FeatureInitParams(), new SharedFeatureSet(), new LogErrorReporter( new ConsoleLogReporter() ) );
		
		if (withNrgStack) {
			session.setNrgStack( NRGStackFixture.create() );
		}
		
		return session;
	}
}
