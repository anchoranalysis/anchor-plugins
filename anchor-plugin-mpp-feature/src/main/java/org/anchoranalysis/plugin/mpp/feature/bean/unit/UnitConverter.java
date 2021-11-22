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

package org.anchoranalysis.plugin.mpp.feature.bean.unit;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.spatial.orientation.DirectionVector;

/**
 * Converts units (distance, area, volume) to a another representation (e.g. physical units)
 *
 * @author Owen Feehan
 */
public class UnitConverter extends AnchorBean<UnitConverter> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean physical = false;

    @BeanField @AllowEmpty @Getter @Setter private String unitType = "";
    // END BEAN PROPERTIES

    /**
     * Convert distance into another unit representation
     *
     * @param value distance expressed as number of voxels
     * @param resolution image-resolution
     * @return distance expressed in desired units
     * @throws FeatureCalculationException
     */
    public double resolveDistance(
            double value, Optional<Resolution> resolution, DirectionVector direction)
            throws FeatureCalculationException {

        // If we aren't doing anything physical, we can just return the current value
        if (isNonPhysical(resolution)) {
            return value;
        }

        return resolution.get().unitConvert().toPhysicalDistance(value, direction, unitType);
    }

    /**
     * Convert volume into another unit representation
     *
     * @param value volume as number of voxels
     * @param resolution image-resolution
     * @return volume expressed in desired units
     * @throws FeatureCalculationException
     */
    public double resolveVolume(double value, Optional<Resolution> resolution)
            throws FeatureCalculationException {

        // If we aren't doing anything physical, we can just return the current value
        if (isNonPhysical(resolution)) {
            return value;
        }

        return resolution.get().unitConvert().toPhysicalVolume(value, unitType);
    }

    /**
     * Convert area into another unit representation
     *
     * @param value area as number of voxels
     * @param resolution image-resolution
     * @return area expressed in desired units
     * @throws FeatureCalculationException
     */
    public double resolveArea(double value, Optional<Resolution> resolution)
            throws FeatureCalculationException {

        // If we aren't doing anything physical, we can just return the current value
        if (isNonPhysical(resolution)) {
            return value;
        }

        return resolution.get().unitConvert().toPhysicalArea(value, unitType);
    }

    /**
     * Do we desire to convert to NON-physical coordinates? If it's physical, check that
     * image-resolution is present as required.
     *
     * @throws FeatureCalculationException if physical is set, but the resolution is not
     * @return true iff the target-units for conversion are non-physical
     */
    private boolean isNonPhysical(Optional<Resolution> res) throws FeatureCalculationException {
        if (physical) {
            checkResIsPresent(res);
            return false;
        } else {
            return true;
        }
    }

    private void checkResIsPresent(Optional<Resolution> res) throws FeatureCalculationException {
        if (!res.isPresent()) {
            throw new FeatureCalculationException(
                    "Image-resolution is required for conversions to physical units, but it is not specified");
        }
    }
}
