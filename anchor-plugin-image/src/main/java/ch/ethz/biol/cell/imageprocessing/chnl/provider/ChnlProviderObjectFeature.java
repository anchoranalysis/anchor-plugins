/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.functional.function.CheckedToIntFunction;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitParams;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.datatype.UnsignedByte;

public class ChnlProviderObjectFeature extends ChnlProviderOneObjectsSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int valueNoObject = 0;

    @BeanField @Getter @Setter private FeatureProvider<FeatureInputSingleObject> feature;

    @BeanField @Getter @Setter
    private List<ChannelProvider> listAdditionalChnlProviders = new ArrayList<>();

    @BeanField @Getter @Setter private double factor = 1.0;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromChannel(Channel chnl, ObjectCollection objectsSource)
            throws CreateException {

        Feature<FeatureInputSingleObject> featureCreated = feature.create();

        try {
            NRGStackWithParams nrgStack = new NRGStackWithParams(createNrgStack(chnl));

            FeatureCalculatorSingle<FeatureInputSingleObject> calculator = createSession(featureCreated);

            return createOutputChnl(
                    chnl.dimensions(),
                    objectsSource,
                    object -> valueToAssignForObject(object, calculator, nrgStack));

        } catch (FeatureCalculationException | InitException e) {
            throw new CreateException(e);
        }
    }

    private NRGStack createNrgStack(Channel chnl) throws CreateException {
        NRGStack nrgStack = new NRGStack(chnl);

        // add other channels
        for (ChannelProvider cp : listAdditionalChnlProviders) {
            Channel chnlAdditional = cp.create();

            if (!chnlAdditional.dimensions().equals(chnl.dimensions())) {
                throw new CreateException(
                        "Dimensions of additional channel are not equal to main channel");
            }

            try {
                nrgStack.asStack().addChannel(chnlAdditional);
            } catch (IncorrectImageSizeException e) {
                throw new CreateException(e);
            }
        }

        return nrgStack;
    }

    private FeatureCalculatorSingle<FeatureInputSingleObject> createSession(
            Feature<FeatureInputSingleObject> feature) throws InitException {
        return FeatureSession.with(
                feature,
                new FeatureInitParams(),
                getInitializationParameters().features().getSharedFeatureSet(),
                getLogger());
    }

    private Channel createOutputChnl(
            ImageDimensions dimensions,
            ObjectCollection objectsSource,
            CheckedToIntFunction<ObjectMask, FeatureCalculationException> valueToAssign)
            throws FeatureCalculationException {
        Channel out =
                ChannelFactory.instance().create(dimensions, UnsignedByte.INSTANCE);
        out.assignValue(valueNoObject).toAll();
        for (ObjectMask object : objectsSource) {
            out.assignValue(valueToAssign.applyAsInt(object)).toObject(object);
        }

        return out;
    }

    private int valueToAssignForObject(
            ObjectMask object,
            FeatureCalculatorSingle<FeatureInputSingleObject> calculator,
            NRGStackWithParams nrgStack)
            throws FeatureCalculationException {
        double featVal = calculator.calculate(new FeatureInputSingleObject(object, nrgStack));
        return (int) (factor * featVal);
    }
}
