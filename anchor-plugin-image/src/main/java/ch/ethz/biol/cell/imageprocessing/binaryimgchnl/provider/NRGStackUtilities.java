/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NRGStackUtilities {

    public static FeatureCalculatorSingle<FeatureInputSingleObject> maybeAddNrgStack(
            FeatureCalculatorSingle<FeatureInputSingleObject> session, Optional<Channel> chnl) {

        if (chnl.isPresent()) {
            return addNrgStack(session, chnl.get());
        } else {
            return session;
        }
    }

    public static FeatureCalculatorSingle<FeatureInputSingleObject> addNrgStack(
            FeatureCalculatorSingle<FeatureInputSingleObject> session, Channel chnl) {

        // Make sure an NRG stack is added to each params that are called
        NRGStackWithParams nrgStack = new NRGStackWithParams(chnl);
        return new FeatureCalculatorSingleChangeInput<>(
                session, params -> params.setNrgStack(nrgStack));
    }
}
