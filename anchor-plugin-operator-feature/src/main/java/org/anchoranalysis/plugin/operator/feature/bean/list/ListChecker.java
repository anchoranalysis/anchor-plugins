/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.list;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ListChecker {

    public static <T extends FeatureInput> void checkNonEmpty(List<Feature<T>> list)
            throws FeatureCalcException {
        if (list.isEmpty()) {
            throw new FeatureCalcException(
                    "There are 0 items in the list so this feature cannot be calculated");
        }
    }
}
