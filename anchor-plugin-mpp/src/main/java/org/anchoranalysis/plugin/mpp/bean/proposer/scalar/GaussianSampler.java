/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.mpp.bean.proposer.ScalarProposer;

/**
 * Samples from a Gaussian distribution whose parameters are determined by a {@link Dictionary}.
 *
 * @author Owen Feehan
 */
public class GaussianSampler extends ScalarProposer {

    // START BEAN PROPERTIES
    /** The dictionary that parameterizes the Gaussian-distribution that is sampled from. */
    @BeanField @Getter @Setter private DictionaryProvider dictionary;

    /** The name of the key in the dictionary with the mean of the distribution. */
    @BeanField @Getter @Setter private String keyMean = "";

    /**
     * The name of the key in the dictionary with the standard-deviation.
     *
     * <p>Ultimately the standard-deviation of the distribution is formed by {@code
     * from(keyStandardDeviation) * factorStandardDeviation}.
     */
    @BeanField @Getter @Setter private String keyStandardDeviation = "";

    /**
     * Multiples the standard deviation of the distribution found in {@code keyStandardDeviation}.
     *
     * <p>Ultimately the standard-deviation of the distribution is formed by {@code
     * from(keyStandardDeviation) * factorStandardDeviation}.
     */
    @BeanField @Getter @Setter private double factorStandardDeviation = 1.0;
    // END BEAN PROPERTIES

    @Override
    public double propose(
            RandomNumberGenerator randomNumberGenerator, Optional<Resolution> resolution)
            throws OperationFailedException {

        try {
            Dictionary dictionaryCreated = dictionary.create();

            if (!dictionaryCreated.containsKey(keyMean)) {
                throw new OperationFailedException(
                        String.format("Dictionary is missing key '%s' for paramMean", keyMean));
            }

            if (!dictionaryCreated.containsKey(keyStandardDeviation)) {
                throw new OperationFailedException(
                        String.format(
                                "Dictionary is missing key '%s' for paramStdDev",
                                keyStandardDeviation));
            }

            double mean = dictionaryCreated.getAsDouble(keyMean);
            double stdDev =
                    dictionaryCreated.getAsDouble(keyStandardDeviation) * factorStandardDeviation;

            return randomNumberGenerator.generateNormal(mean, stdDev).nextDouble();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
