/* (C)2020 */
package ch.ethz.biol.cell.mpp.pair.addcriteria;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.AddCriteriaPair;
import org.anchoranalysis.anchor.mpp.feature.addcriteria.IncludeMarksFailureException;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.extent.ImageDimensions;

public class AddCriteriaFeatureRelationThreshold extends AddCriteriaPair {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Feature<FeatureInputPairMemo> feature;

    @BeanField @Getter @Setter private double threshold;

    @BeanField @Getter @Setter private RelationBean relation;
    // END BEAN PROPERTIES

    @Override
    public boolean includeMarks(
            VoxelizedMarkMemo mark1,
            VoxelizedMarkMemo mark2,
            ImageDimensions dimensions,
            Optional<FeatureCalculatorMulti<FeatureInputPairMemo>> session,
            boolean do3D)
            throws IncludeMarksFailureException {

        try {
            FeatureInputPairMemo params =
                    new FeatureInputPairMemo(mark1, mark2, new NRGStackWithParams(dimensions));

            double featureVal =
                    session.orElseThrow(() -> new IncludeMarksFailureException("No session exists"))
                            .calc(params, FeatureListFactory.from(feature))
                            .get(0);

            return relation.create().isRelationToValueTrue(featureVal, threshold);

        } catch (FeatureCalcException e) {
            throw new IncludeMarksFailureException(e);
        }
    }

    @Override
    public Optional<FeatureList<FeatureInputPairMemo>> orderedListOfFeatures() {
        return Optional.of(FeatureListFactory.from(feature));
    }
}
