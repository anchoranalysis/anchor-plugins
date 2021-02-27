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

package org.anchoranalysis.plugin.image.bean.channel.provider.score;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.functional.checked.CheckedToIntFunction;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitialization;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.single.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.channel.provider.UnaryWithObjectsBase;

public class ScoreObjects extends UnaryWithObjectsBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int valueNoObject = 0;

    /** Feature that calculates the score for an object. */
    @BeanField @Getter @Setter @SkipInit private Feature<FeatureInputSingleObject> feature;

    @BeanField @Getter @Setter
    private List<ChannelProvider> listAdditionalChannelProviders = new ArrayList<>();

    @BeanField @Getter @Setter private double factor = 1.0;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromChannel(Channel channel, ObjectCollection objects)
            throws CreateException {

        try {
            EnergyStack energyStack = new EnergyStack(createEnergyStack(channel));

            FeatureCalculatorSingle<FeatureInputSingleObject> calculator = createSession(feature);

            return createOutputChannel(
                    channel.dimensions(),
                    objects,
                    object -> valueToAssignForObject(object, calculator, energyStack));

        } catch (FeatureCalculationException | InitException e) {
            throw new CreateException(e);
        }
    }

    private EnergyStackWithoutParams createEnergyStack(Channel channel) throws CreateException {
        EnergyStackWithoutParams energyStack = new EnergyStackWithoutParams(channel);

        // add other channels
        for (ChannelProvider cp : listAdditionalChannelProviders) {
            Channel channelAdditional = cp.create();

            if (!channelAdditional.dimensions().equals(channel.dimensions())) {
                throw new CreateException(
                        "Dimensions of additional channel are not equal to main channel");
            }

            try {
                energyStack.asStack().addChannel(channelAdditional);
            } catch (IncorrectImageSizeException e) {
                throw new CreateException(e);
            }
        }

        return energyStack;
    }

    private FeatureCalculatorSingle<FeatureInputSingleObject> createSession(
            Feature<FeatureInputSingleObject> feature) throws InitException {
        return FeatureSession.with(
                feature,
                new FeatureInitialization(),
                getInitialization().featuresInitParams().getSharedFeatures(),
                getLogger());
    }

    private Channel createOutputChannel(
            Dimensions dimensions,
            ObjectCollection objectsSource,
            CheckedToIntFunction<ObjectMask, FeatureCalculationException> valueToAssign)
            throws FeatureCalculationException {
        Channel out = ChannelFactory.instance().create(dimensions, UnsignedByteVoxelType.INSTANCE);
        out.assignValue(valueNoObject).toAll();
        for (ObjectMask object : objectsSource) {
            out.assignValue(valueToAssign.applyAsInt(object)).toObject(object);
        }

        return out;
    }

    private int valueToAssignForObject(
            ObjectMask object,
            FeatureCalculatorSingle<FeatureInputSingleObject> calculator,
            EnergyStack energyStack)
            throws FeatureCalculationException {
        double featVal = calculator.calculate(new FeatureInputSingleObject(object, energyStack));
        return (int) (factor * featVal);
    }
}
