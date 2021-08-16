/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature;

import static org.anchoranalysis.test.feature.plugins.ResultsVectorTestUtilities.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.multi.FeatureCalculatorMulti;
import org.anchoranalysis.image.feature.input.FeatureInputHistogram;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.ConstantsInListFixture;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;
import org.anchoranalysis.test.feature.plugins.HistogramFixture;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.object.CutOffCornersObjectFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test is located in this package, as it uses BeanXML in resources that depends on plugins in
 * this module
 *
 * @author Owen Feehan
 */
class FeatureListImageTest {

    private static TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    private static EnergyStack ENERGY_STACK = EnergyStackFixture.create(true, true);

    @BeforeEach
    void setUp() {
        RegisterBeanFactories.registerAllPackageBeanFactories();
    }

    @Test
    void testNoParams() {

        assertThrows(
                NamedFeatureCalculateException.class,
                () -> {
                    FeatureCalculatorMulti<FeatureInput> session =
                            createAndStart(ConstantsInListFixture.create());

                    session.calculate((FeatureInput) null);
                });
    }

    @Test
    void testHistogram()
            throws InitException, FeatureCalculationException, CreateException,
                    NamedFeatureCalculateException {

        FeatureCalculatorMulti<FeatureInputHistogram> session =
                createAndStart(histogramFeatures(loader));

        assertCalc(
                session.calculate(createParams(HistogramFixture.createAscending())),
                32450.0,
                30870.0,
                14685.0,
                140.0,
                214.0,
                0.005545343137254902);

        assertCalc(
                session.calculate(createParams(HistogramFixture.createDescending())),
                27730.0,
                19110.0,
                2145.0,
                41.0,
                115.0,
                0.0022671568627450982);
    }

    @Test
    void testImage()
            throws InitException, NamedFeatureCalculateException, CreateException,
                    FeatureCalculationException {

        FeatureCalculatorMulti<FeatureInputSingleObject> session =
                createAndStart(objectFeatures(loader));

        CutOffCornersObjectFixture objectFixture =
                new CutOffCornersObjectFixture(ENERGY_STACK.dimensions());

        assertCalculateObject(
                session,
                objectFixture.create1(),
                31.5,
                29.0,
                3.0,
                59.0,
                0.07142857142857142,
                2744.0,
                560.0);

        assertCalculateObject(
                session,
                objectFixture.create2(),
                7.5,
                21.0,
                7.0,
                28.5,
                0.32142857142857145,
                108.0,
                66.0);

        assertCalculateObject(
                session,
                objectFixture.create3(),
                21.5,
                35.0,
                2.0,
                55.5,
                1.6996904024767803,
                612.0,
                162.0);
    }

    private <T extends FeatureInput> FeatureCalculatorMulti<T> createAndStart(
            FeatureList<T> features) throws InitException {
        return FeatureSession.with(features, LoggingFixture.suppressedLogger());
    }

    /**
     * creates a feature-list associated with the fixture
     *
     * @throws CreateException
     */
    private static FeatureList<FeatureInputHistogram> histogramFeatures(TestLoader loader)
            throws CreateException {
        return FeaturesFromXmlFixture.createFeatureList("histogramFeatureList.xml", loader);
    }

    /**
     * creates a feature-list associated with obj-mask
     *
     * @throws CreateException
     */
    private static FeatureList<FeatureInputSingleObject> objectFeatures(TestLoader loader)
            throws CreateException {
        return FeaturesFromXmlFixture.createFeatureList("objectFeatureList.xml", loader);
    }

    private static FeatureInputHistogram createParams(Histogram histogram) throws CreateException {
        return new FeatureInputHistogram(histogram, ENERGY_STACK.resolution());
    }

    private static void assertCalculateObject(
            FeatureCalculatorMulti<FeatureInputSingleObject> session,
            ObjectMask objectMaskToCalculate,
            Object... expectedVals)
            throws NamedFeatureCalculateException, CreateException {
        assertCalc(
                session.calculate(
                        new FeatureInputSingleObject(objectMaskToCalculate, ENERGY_STACK)),
                expectedVals);
    }
}
