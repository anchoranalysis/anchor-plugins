/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.mpp.feature.bean;

import static org.anchoranalysis.test.feature.plugins.ResultsVectorTestUtilities.*;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.collection.FeatureInputMarkCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkCollection;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.calculate.results.ResultsVector;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.ConstantsInListFixture;
import org.anchoranalysis.test.feature.plugins.ResultsVectorTestUtilities;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.junit.Before;
import org.junit.Test;

public class FeatureListMPPTest {

    private static final EnergyStack energyStack = EnergyStackFixture.create(false, true);
    private static final Dimensions DIM = energyStack.dimensions();

    @Before
    public void setUp() {
        RegisterBeanFactories.registerAllPackageBeanFactories();
    }

    @Test(expected = NamedFeatureCalculateException.class)
    public void testNoParams()
            throws InitException, NamedFeatureCalculateException, CreateException {

        testConstantsInList((FeatureInput) null, (FeatureInput) null);
    }

    @Test
    public void testArbitraryParams()
            throws InitException, NamedFeatureCalculateException, CreateException {

        MarkCollectionFixture marksFixture = new MarkCollectionFixture(DIM);

        testConstantsInList(
                new FeatureInputMarkCollection(marksFixture.createMarks1(), Optional.of(DIM)),
                new FeatureInputMarkCollection(marksFixture.createMarks2(), Optional.of(DIM)));
    }

    @Test
    public void testMark() throws InitException, CreateException, NamedFeatureCalculateException {

        FeatureCalculatorMulti<FeatureInputMark> session =
                createAndStart(FeatureListFixtureMPP.mark());

        MarkFixture markFixture = new MarkFixture(DIM);

        assertMark(
                session,
                markFixture.createEllipsoid1(),
                0.8842716428906386,
                6.97330619981458,
                0.9408494125082603);

        assertMark(
                session,
                markFixture.createEllipsoid2(),
                0.7605449154770859,
                3.48665309990729,
                0.9408494125082603);

        assertMark(
                session,
                markFixture.createEllipsoid3(),
                0.8585645700992556,
                1.8226663204715146,
                0.22330745949001382);
    }

    @Test
    public void testCfg()
            throws InitException, CreateException, FeatureCalculationException,
                    NamedFeatureCalculateException {

        FeatureCalculatorMulti<FeatureInputMarkCollection> session =
                createAndStart(FeatureListFixtureMPP.cfg());

        MarkCollectionFixture cfgFixture = new MarkCollectionFixture(DIM);

        assertCfg(session, cfgFixture.createMarks1(), 2.0);
        assertCfg(session, cfgFixture.createMarks2(), 3.0);
        assertCfg(session, cfgFixture.createMarks3(), 2.0);
        assertCfg(session, cfgFixture.createMarkSingle(), 1.0);
        assertCfg(session, new MarkCollection(), 0.0);
    }

    private static void assertCfg(
            FeatureCalculatorMulti<FeatureInputMarkCollection> session, MarkCollection cfg, double expected)
            throws CreateException, FeatureCalculationException, NamedFeatureCalculateException {
        assertCalc(session.calculate(new FeatureInputMarkCollection(cfg, Optional.of(DIM))), expected);
    }

    private static void assertMark(
            FeatureCalculatorMulti<FeatureInputMark> session,
            Mark mark,
            double expected1,
            double expected2,
            double expected3)
            throws CreateException, NamedFeatureCalculateException {
        ResultsVector rv = session.calculate(new FeatureInputMark(mark, Optional.of(DIM)));
        ResultsVectorTestUtilities.assertCalc(rv, expected1, expected2, expected3);
    }

    private static <T extends FeatureInput> FeatureCalculatorMulti<T> createAndStart(
            FeatureList<T> features) throws InitException {
        return FeatureSession.with(features, LoggingFixture.suppressedLogErrorReporter());
    }

    private void testConstantsInList(FeatureInput params1, FeatureInput params2)
            throws CreateException, InitException, NamedFeatureCalculateException {

        FeatureCalculatorMulti<FeatureInput> session =
                createAndStart(ConstantsInListFixture.create());

        ResultsVector rv1 = session.calculate(params1);
        ConstantsInListFixture.checkResultVector(rv1);

        ResultsVector rv2 = session.calculate(params2);
        ConstantsInListFixture.checkResultVector(rv2);
    }
}
