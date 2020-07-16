/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.physical;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputWithRes;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.unitvalue.areavolume.UnitValueAreaOrVolume;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolumeVoxels;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.feature.bean.physical.FeatureSingleElemWithRes;

/**
 * Checks if a value lies within a range defined by units (a minimum and maximum boundary)
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public class UnitsWithinRange<T extends FeatureInputWithRes> extends FeatureSingleElemWithRes<T> {

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
    @BeanField @Getter @Setter private UnitValueAreaOrVolume min = new UnitValueVolumeVoxels(0);

    /**
     * Maximum-boundary for acceptable range
     *
     * <p>We default to volume as units, but it could also be area. It's arbitrary for
     * infinity-value.
     */
    @BeanField @Getter @Setter
    private UnitValueAreaOrVolume max = new UnitValueVolumeVoxels(Double.MAX_VALUE);
    // END BEAN PROPERTIES

    @Override
    protected double calcWithRes(double value, ImageResolution res) throws FeatureCalcException {

        try {
            Optional<ImageResolution> resOpt = Optional.of(res);
            double minVoxels = min.resolveToVoxels(resOpt);
            double maxVoxels = max.resolveToVoxels(resOpt);

            if (value >= minVoxels && value <= maxVoxels) {
                return within;
            } else {
                return outside;
            }

        } catch (UnitValueException e) {
            throw new FeatureCalcException(e);
        }
    }

    @Override
    public String getParamDscr() {
        return String.format("min=%s,max=%s,within=%8.3f outside=%8.3f", min, max, within, outside);
    }
}
