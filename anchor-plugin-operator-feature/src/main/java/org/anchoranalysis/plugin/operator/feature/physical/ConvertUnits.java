/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.physical;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.unit.SpatialConversionUtilities;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

//
public class ConvertUnits<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

    // START BEAN PROPERTIES
    @BeanField @AllowEmpty @Getter @Setter private String unitTypeFrom = "";

    @BeanField @AllowEmpty @Getter @Setter private String unitTypeTo = "";
    // END BEAN PROPERTIES

    @Override
    protected double calc(SessionInput<T> input) throws FeatureCalcException {

        double value = input.calc(getItem());

        SpatialConversionUtilities.UnitSuffix typeFrom =
                SpatialConversionUtilities.suffixFromMeterString(unitTypeFrom);
        SpatialConversionUtilities.UnitSuffix typeTo =
                SpatialConversionUtilities.suffixFromMeterString(unitTypeTo);

        double valueBaseUnits = SpatialConversionUtilities.convertFromUnits(value, typeFrom);
        return SpatialConversionUtilities.convertToUnits(valueBaseUnits, typeTo);
    }
}
