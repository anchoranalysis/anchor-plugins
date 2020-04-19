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

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureCfgParams;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMarkParams;
import org.anchoranalysis.anchor.mpp.mark.Mark;
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
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.ConstantsInListFixture;
import org.anchoranalysis.test.feature.plugins.NRGStackFixture;
import org.anchoranalysis.test.feature.plugins.ResultsVectorTestUtilities;
import org.junit.Before;
import org.junit.Test;

public class FeatureListMPPTest {
	
	private static final NRGStackWithParams nrgStack = NRGStackFixture.create(false,true);
	private static final ImageRes RES = nrgStack.getDimensions().getRes();
	private static final ImageDim DIM = nrgStack.getDimensions();
	
	@Before
    public void setUp() {
		RegisterBeanFactories.registerAllPackageBeanFactories();
    }
	
	@Test(expected = FeatureCalcException.class)
	public void testNoParams() throws InitException, FeatureCalcException, CreateException {
		
		testConstantsInList(
			(FeatureCalcParams) null,
			(FeatureCalcParams) null
		);
	}
	
	@Test
	public void testArbitraryParams() throws InitException, FeatureCalcException, CreateException {
		
		CfgFixture cfgFixture = new CfgFixture(DIM);
		
		testConstantsInList(
			new FeatureCfgParams(cfgFixture.createCfg1(), DIM ),
			new FeatureCfgParams(cfgFixture.createCfg2(), DIM )
		);
	}
		
	@Test
	public void testMark() throws InitException, CreateException, FeatureCalcException {
		
		FeatureCalculatorMulti<FeatureMarkParams> session = createAndStart( FeatureListFixtureMPP.mark() );
		
		MarkFixture markFixture = new MarkFixture( DIM );
		
		assertMark(
			session,
			markFixture.createEllipsoid1(),
			0.8842716428906386,
			6.97330619981458,
			0.9408494125082603				
		);
		
		assertMark(
			session,
			markFixture.createEllipsoid2(),
			0.7605449154770859,
			3.48665309990729,
			0.9408494125082603				
		);
		
		assertMark(
			session,
			markFixture.createEllipsoid3(),
			0.8585645700992556,
			1.8226663204715146,
			0.22330745949001382				
		);
	}
	
	@Test
	public void testCfg() throws InitException, CreateException, FeatureCalcException {
		
		FeatureCalculatorMulti<FeatureCfgParams> session = createAndStart( FeatureListFixtureMPP.cfg() );
		
		CfgFixture cfgFixture = new CfgFixture(DIM);
		
		assertCfg(session, cfgFixture.createCfg1(), 2.0);
		assertCfg(session, cfgFixture.createCfg2(), 3.0);
		assertCfg(session, cfgFixture.createCfg3(), 2.0);
		assertCfg(session, cfgFixture.createCfgSingle(), 1.0);
		assertCfg(session, new Cfg(), 0.0);
	}
	
	private static void assertCfg( FeatureCalculatorMulti<FeatureCfgParams> session, Cfg cfg, double expected ) throws CreateException, FeatureCalcException {
		assertCalc(
			session.calc(
				new FeatureCfgParams(cfg, DIM )
			),
			expected
		);
	}

	private static void assertMark( FeatureCalculatorMulti<FeatureMarkParams> session, Mark mark, double expected1, double expected2, double expected3 ) throws CreateException, FeatureCalcException {
		ResultsVector rv = session.calc(
			new FeatureMarkParams(mark, RES )
		); 
		ResultsVectorTestUtilities.assertCalc(
			rv,
			expected1,
			expected2,
			expected3
		);
	}
	
	private static <T extends FeatureCalcParams> FeatureCalculatorMulti<T> createAndStart( FeatureList<T> features ) throws FeatureCalcException {
		return SessionFactory.createAndStart(
			features,
			LoggingFixture.simpleLogErrorReporter()
		);
	}
	
	private void testConstantsInList( FeatureCalcParams params1, FeatureCalcParams params2 ) throws FeatureCalcException, CreateException, InitException {
		
		FeatureCalculatorMulti<FeatureCalcParams> session = createAndStart(ConstantsInListFixture.create());
		
		ResultsVector rv1 = session.calc(params1);
		ConstantsInListFixture.checkResultVector(rv1);
		
		ResultsVector rv2 = session.calc(params2);
		ConstantsInListFixture.checkResultVector(rv2);
	}
}
