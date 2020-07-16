/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.properties.ObjectWithProperties;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateSingleObjFromMemo
        extends FeatureCalculation<FeatureInputSingleObject, FeatureInputSingleMemo> {

    private final RegionMap regionMap;
    private final int index;

    @Override
    protected FeatureInputSingleObject execute(FeatureInputSingleMemo input)
            throws FeatureCalcException {
        return new FeatureInputSingleObject(calcMask(input), input.getNrgStackOptional());
    }

    private ObjectMask calcMask(FeatureInputSingleMemo params) throws FeatureCalcException {
        ObjectWithProperties om =
                params.getPxlPartMemo()
                        .getMark()
                        .calcMask(
                                params.getDimensionsRequired(),
                                regionMap.membershipWithFlagsForIndex(index),
                                BinaryValuesByte.getDefault());
        return om.getMask();
    }
}
