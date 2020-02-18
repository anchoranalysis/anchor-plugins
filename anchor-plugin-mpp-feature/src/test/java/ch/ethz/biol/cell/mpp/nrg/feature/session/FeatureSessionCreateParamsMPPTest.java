package ch.ethz.biol.cell.mpp.nrg.feature.session;

/*-
 * #%L
 * anchor-plugin-mpp-feature
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

import static org.anchoranalysis.test.feature.plugins.ResultsVectorTestUtilities.*;

import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.log.ConsoleLogReporter;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.test.feature.SimpleFeatureListFixture;
import org.anchoranalysis.test.feature.plugins.NRGStackFixture;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.biol.cell.mpp.cfg.Cfg;

public class FeatureSessionCreateParamsMPPTest {
	
	@Before
    public void setUp() {
		RegisterBeanFactories.registerAllPackageBeanFactories(false);
    }
	
	@Test
	public void testNoParams() throws InitException, FeatureCalcException, CreateException {
		
		FeatureSessionCreateParamsMPP session = createAndStart(SimpleFeatureListFixture.create());
		
		ResultsVector rv1 = session.calc();
		SimpleFeatureListFixture.checkResultVector(rv1);
		
		ResultsVector rv2 = session.calc();
		SimpleFeatureListFixture.checkResultVector(rv2);
	}
	
	@Test
	public void testMark() throws InitException, CreateException, FeatureCalcException {
		
		FeatureSessionCreateParamsMPP session = createAndStart( FeatureListFixtureMPP.mark() );
		
		MarkFixture markFixture = new MarkFixture( session.getNrgStack().getDimensions() );
		
		assertCalc(
			session.calc( markFixture.createEllipsoid1() ),
			0.8842716428906386, 6.97330619981458, 0.9408494125082603				
		);
		
		assertCalc(
			session.calc( markFixture.createEllipsoid2() ),
			0.7605449154770859, 3.48665309990729, 0.9408494125082603				
		);
		
		assertCalc(
			session.calc( markFixture.createEllipsoid3() ),
			0.8585645700992556, 1.8226663204715146, 0.22330745949001382				
		);
	}
	
	@Test
	public void testCfg() throws InitException, CreateException, FeatureCalcException {
		
		FeatureSessionCreateParamsMPP session = createAndStart( FeatureListFixtureMPP.cfg() );
		
		CfgFixture cfgFixture = new CfgFixture( session.getNrgStack().getDimensions() );
		
		assertCalc(
			session.calc( cfgFixture.createCfg1() ),
			2.0				
		);
		
		assertCalc(
			session.calc( cfgFixture.createCfg2() ),
			3.0				
		);
		
		assertCalc(
			session.calc( cfgFixture.createCfg3() ),
			2.0			
		);
		
		assertCalc(
			session.calc( cfgFixture.createCfgSingle() ),
			1.0				
		);
		
		assertCalc(
			session.calc( new Cfg() ),
			0.0			
		);
	}
	
	private FeatureSessionCreateParamsMPP createAndStart( FeatureList features ) throws InitException, CreateException {
		NRGStackWithParams nrgStack = NRGStackFixture.create();
		FeatureSessionCreateParamsMPP session = new FeatureSessionCreateParamsMPP(features, nrgStack.getNrgStack(), nrgStack.getParams()  );
		session.start( new FeatureInitParams(), new SharedFeatureSet(), new LogErrorReporter( new ConsoleLogReporter() ) );
		return session;
	}
}
