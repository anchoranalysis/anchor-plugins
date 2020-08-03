package org.anchoranalysis.plugin.operator.feature.score;

import org.anchoranalysis.feature.calc.FeatureCalculationException;

/**
 * Supplies the result of a feature-calculation
 * 
 * @author Owen Feehan
 *
 */
@FunctionalInterface
public interface FeatureResultSupplier {

    double get() throws FeatureCalculationException;
}
