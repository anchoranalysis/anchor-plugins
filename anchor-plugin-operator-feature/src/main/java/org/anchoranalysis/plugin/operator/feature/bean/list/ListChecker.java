package org.anchoranalysis.plugin.operator.feature.bean.list;

import java.util.List;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

class ListChecker {

	private ListChecker() {}

	public static <T extends FeatureInput> void checkNonEmpty( List<Feature<T>> list ) throws FeatureCalcException {
		if (list.size()==0) {
			throw new FeatureCalcException("There are 0 items in the list so this feature cannot be calculated");
		}
	}
}
