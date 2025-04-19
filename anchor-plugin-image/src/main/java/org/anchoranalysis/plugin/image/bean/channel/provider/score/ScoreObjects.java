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
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.functional.checked.CheckedToIntFunction;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingle;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.feature.initialization.FeatureInitialization;
import org.anchoranalysis.feature.session.FeatureSession;
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

/**
 * Creates a channel by scoring objects based on a feature calculation.
 *
 * <p>This class assigns intensity values to voxels in a new channel based on feature calculations
 * performed on objects in the input channel.
 */
public class ScoreObjects extends UnaryWithObjectsBase {

    // START BEAN PROPERTIES
    /** The value assigned to voxels that do not belong to any object. */
    @BeanField @Getter @Setter private int valueNoObject = 0;

    /** Feature that calculates the score for an object. */
    @BeanField @Getter @Setter @SkipInit private Feature<FeatureInputSingleObject> feature;

    /** Additional channels to be included in the {@link EnergyStack} for feature calculation. */
    @BeanField @Getter @Setter
    private List<ChannelProvider> listAdditionalChannelProviders = new ArrayList<>();

    /** Factor to multiply the feature value by before assigning it to the output channel. */
    @BeanField @Getter @Setter private double factor = 1.0;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromChannel(Channel channel, ObjectCollection objects)
            throws ProvisionFailedException {

        try {
            EnergyStack energyStack = new EnergyStack(createEnergyStack(channel));

            FeatureCalculatorSingle<FeatureInputSingleObject> calculator = createSession(feature);

            return createOutputChannel(
                    channel.dimensions(),
                    objects,
                    object -> valueToAssignForObject(object, calculator, energyStack));

        } catch (FeatureCalculationException | InitializeException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Creates an {@link EnergyStackWithoutParameters} from the input channel and additional
     * channels.
     *
     * @param channel the input channel
     * @return the created {@link EnergyStackWithoutParameters}
     * @throws ProvisionFailedException if there's an error creating the energy stack
     */
    private EnergyStackWithoutParameters createEnergyStack(Channel channel)
            throws ProvisionFailedException {
        EnergyStackWithoutParameters energyStack = new EnergyStackWithoutParameters(channel);

        // add other channels
        for (ChannelProvider cp : listAdditionalChannelProviders) {
            Channel channelAdditional = cp.get();

            if (!channelAdditional.dimensions().equals(channel.dimensions())) {
                throw new ProvisionFailedException(
                        "Dimensions of additional channel are not equal to main channel");
            }

            try {
                energyStack.asStack().addChannel(channelAdditional);
            } catch (IncorrectImageSizeException e) {
                throw new ProvisionFailedException(e);
            }
        }

        return energyStack;
    }

    /**
     * Creates a {@link FeatureCalculatorSingle} for the given feature.
     *
     * @param feature the feature to create a calculator for
     * @return the created {@link FeatureCalculatorSingle}
     * @throws InitializeException if there's an error initializing the feature calculator
     */
    private FeatureCalculatorSingle<FeatureInputSingleObject> createSession(
            Feature<FeatureInputSingleObject> feature) throws InitializeException {
        return FeatureSession.with(
                feature,
                new FeatureInitialization(),
                getInitialization().featuresInitialization().getSharedFeatures(),
                getLogger());
    }

    /**
     * Creates an output channel by assigning values to objects based on the provided function.
     *
     * @param dimensions the dimensions of the output channel
     * @param objectsSource the collection of objects to score
     * @param valueToAssign the function that determines the value to assign to each object
     * @return the created output {@link Channel}
     * @throws FeatureCalculationException if there's an error during feature calculation
     */
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

    /**
     * Calculates the value to assign to an object based on feature calculation.
     *
     * @param object the object to calculate the value for
     * @param calculator the feature calculator
     * @param energyStack the energy stack for feature calculation
     * @return the calculated value to assign to the object
     * @throws FeatureCalculationException if there's an error during feature calculation
     */
    private int valueToAssignForObject(
            ObjectMask object,
            FeatureCalculatorSingle<FeatureInputSingleObject> calculator,
            EnergyStack energyStack)
            throws FeatureCalculationException {
        double featVal = calculator.calculate(new FeatureInputSingleObject(object, energyStack));
        return (int) (factor * featVal);
    }
}
