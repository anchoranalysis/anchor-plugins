/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.extent.ImageDimensions;

public class MarkProviderRequireFeatureRelationThreshold extends MarkProvider {

    // START BEAN PROPERTIES
    @BeanField private MarkProvider markProvider;

    @BeanField private FeatureProvider<FeatureInputMark> featureProvider;

    @BeanField private double threshold;

    @BeanField private RelationBean relation;

    @BeanField @OptionalBean private ImageDimProvider dim = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> create() throws CreateException {

        Optional<Mark> mark = markProvider.create();
        return OptionalUtilities.flatMap(
                mark,
                m -> {
                    if (isFeatureAccepted(m)) {
                        return mark;
                    } else {
                        return Optional.empty();
                    }
                });
    }

    private boolean isFeatureAccepted(Mark mark) throws CreateException {
        Feature<FeatureInputMark> feature = featureProvider.create();

        double featureVal = calculateInput(feature, mark);

        return relation.create().isRelationToValueTrue(featureVal, threshold);
    }

    private Optional<ImageDimensions> dimensions() throws CreateException {
        if (dim != null) {
            return Optional.of(dim.create());
        } else {
            return Optional.empty();
        }
    }

    private double calculateInput(Feature<FeatureInputMark> feature, Mark mark)
            throws CreateException {

        FeatureInputMark input = new FeatureInputMark(mark, dimensions());

        try {
            FeatureCalculatorSingle<FeatureInputMark> session =
                    FeatureSession.with(
                            feature,
                            new FeatureInitParams(),
                            getInitializationParameters().getFeature().getSharedFeatureSet(),
                            getLogger());
            return session.calc(input);

        } catch (FeatureCalcException e) {
            throw new CreateException(e);
        }
    }

    public MarkProvider getMarkProvider() {
        return markProvider;
    }

    public void setMarkProvider(MarkProvider markProvider) {
        this.markProvider = markProvider;
    }

    public FeatureProvider<FeatureInputMark> getFeatureProvider() {
        return featureProvider;
    }

    public void setFeatureProvider(FeatureProvider<FeatureInputMark> featureProvider) {
        this.featureProvider = featureProvider;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public RelationBean getRelation() {
        return relation;
    }

    public void setRelation(RelationBean relation) {
        this.relation = relation;
    }

    public ImageDimProvider getDim() {
        return dim;
    }

    public void setDim(ImageDimProvider dim) {
        this.dim = dim;
    }
}
