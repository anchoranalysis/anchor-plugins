/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;

// Treats the entire binaryimgchnl as an object, and sees if it passes an {@link ObjectFilter}
public class BinaryChnlProviderFeatureRelationAsObjects extends BinaryChnlProviderChnlSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinaryChnlProvider binaryChnlMain;

    @BeanField @Getter @Setter private BinaryChnlProvider binaryChnlCompareTo;

    @BeanField @Getter @Setter private BinaryChnlProvider binaryChnlElse;

    @BeanField @Getter @Setter private FeatureProvider<FeatureInputSingleObject> featureProvider;

    @BeanField @Getter @Setter private RelationBean relation;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromSource(Channel chnlSource) throws CreateException {

        Mask channel = binaryChnlMain.create();

        ObjectMask objectMain = CreateFromEntireChnlFactory.createObject(channel);
        ObjectMask objectCompareTo =
                CreateFromEntireChnlFactory.createObject(binaryChnlCompareTo.create());

        FeatureCalculatorSingle<FeatureInputSingleObject> session = createSession();

        return calcRelation(
                objectMain,
                objectCompareTo,
                channel,
                NRGStackUtilities.addNrgStack(session, chnlSource));
    }

    private FeatureCalculatorSingle<FeatureInputSingleObject> createSession()
            throws CreateException {
        try {
            return FeatureSession.with(
                    featureProvider.create(),
                    new FeatureInitParams(),
                    getInitializationParameters().getFeature().getSharedFeatureSet(),
                    getLogger());
        } catch (FeatureCalcException e1) {
            throw new CreateException(e1);
        }
    }

    private Mask calcRelation(
            ObjectMask objectMain,
            ObjectMask objectCompareTo,
            Mask chnlMain,
            FeatureCalculatorSingle<FeatureInputSingleObject> calculator)
            throws CreateException {
        try {
            double valMain = calculator.calc(new FeatureInputSingleObject(objectMain));
            double valCompareTo = calculator.calc(new FeatureInputSingleObject(objectCompareTo));

            if (relation.create().isRelationToValueTrue(valMain, valCompareTo)) {
                return chnlMain;
            } else {
                return binaryChnlElse.create();
            }
        } catch (FeatureCalcException e) {
            throw new CreateException(e);
        }
    }
}
