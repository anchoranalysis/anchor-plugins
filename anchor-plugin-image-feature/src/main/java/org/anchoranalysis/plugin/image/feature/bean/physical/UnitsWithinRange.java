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

package org.anchoranalysis.plugin.image.feature.bean.physical;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInputWithResolution;
import org.anchoranalysis.image.bean.nonbean.UnitValueException;
import org.anchoranalysis.image.bean.unitvalue.extent.UnitValueExtent;
import org.anchoranalysis.image.bean.unitvalue.extent.volume.VolumeVoxels;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.feature.bean.physical.WithResolutionBase;

/**
 * Checks if a value lies within a range defined by units (a minimum and maximum boundary)
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public class UnitsWithinRange<T extends FeatureInputWithResolution> extends WithResolutionBase<T> {

    // START BEAN PROPERTIES
    /** Returned as a constant if a value lies within the range */
    @BeanField @Getter @Setter private double within = 0;

    /** Returned as a constant if a value lies otside the range */
    @BeanField @Getter @Setter private double outside = -10;

    /**
     * Minimum-boundary for acceptable range
     *
     * <p>We default to volume as units, but it could also be area. It's arbitrary for 0-value.
     */
    @BeanField @Getter @Setter private UnitValueExtent min = new VolumeVoxels(0);

    /**
     * Maximum-boundary for acceptable range
     *
     * <p>We default to volume as units, but it could also be area. It's arbitrary for
     * infinity-value.
     */
    @BeanField @Getter @Setter private UnitValueExtent max = new VolumeVoxels(Double.MAX_VALUE);

    // END BEAN PROPERTIES

    @Override
    protected double calculateWithResolution(double value, Resolution resolution)
            throws FeatureCalculationException {

        try {
            Optional<UnitConverter> unitConverter = Optional.of(resolution.unitConvert());
            double minVoxels = min.resolveToVoxels(unitConverter);
            double maxVoxels = max.resolveToVoxels(unitConverter);

            if (value >= minVoxels && value <= maxVoxels) {
                return within;
            } else {
                return outside;
            }

        } catch (UnitValueException e) {
            throw new FeatureCalculationException(e);
        }
    }

    @Override
    public String describeParameters() {
        return String.format("min=%s,max=%s,within=%8.3f outside=%8.3f", min, max, within, outside);
    }
}
