/* (C)2020 */
package ch.ethz.biol.cell.mpp.pair.addcriteria;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.AddCriteriaPair;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.IncludeMarksFailureException;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.mark.MarkDistance;
import org.anchoranalysis.anchor.mpp.mark.UnsupportedMarkTypeException;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageDimensions;

public class AddCriteriaDistanceTo extends AddCriteriaPair {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private UnitValueDistance threshold;

    @BeanField @Getter @Setter private MarkDistance distance;
    // END BEAN PROPERTIES

    @Override
    public boolean includeMarks(
            VoxelizedMarkMemo mark1,
            VoxelizedMarkMemo mark2,
            ImageDimensions dimensions,
            Optional<FeatureCalculatorMulti<FeatureInputPairMemo>> session,
            boolean do3D)
            throws IncludeMarksFailureException {
        double d;
        try {
            d = distance.distance(mark1.getMark(), mark2.getMark());
        } catch (UnsupportedMarkTypeException e) {
            throw new IncludeMarksFailureException(e);
        }

        try {
            double thresholdVal =
                    threshold.resolve(
                            Optional.of(dimensions.getRes()),
                            mark1.getMark().centerPoint(),
                            mark2.getMark().centerPoint());
            return d < thresholdVal;

        } catch (OperationFailedException e) {
            throw new IncludeMarksFailureException(e);
        }
    }

    @Override
    public Optional<FeatureList<FeatureInputPairMemo>> orderedListOfFeatures() {
        return Optional.empty();
    }
}
