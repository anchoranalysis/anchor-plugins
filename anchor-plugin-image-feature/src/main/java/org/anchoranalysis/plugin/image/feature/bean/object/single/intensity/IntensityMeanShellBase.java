/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.morphological.MorphologicalIterations;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateShellObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Constructs a shell around an object-mask using a standard dilation and erosion process.
 *
 * @author Owen Feehan
 */
public abstract class IntensityMeanShellBase extends FeatureEnergyChannel {

    // START BEAN PROPERTIES
    /** The number of dilations and erosions to apply and whether to do in the Z dimension. */
    @BeanField @Getter @Setter
    private MorphologicalIterations iterations = new MorphologicalIterations();

    /**
     * If true, calculates instead on the inverse of the object-mask (what's left when the shell is
     * removed).
     */
    @BeanField @Getter @Setter private boolean inverse = false;

    /**
     * A channel of the energyStack that is used as an additional mask using default byte values for
     * <i>on</i> and <i>off</i>.
     */
    @BeanField @Getter @Setter private int energyIndexMask = -1;

    /** If true, uses the inverse of the passed mask. */
    @BeanField @Getter @Setter private boolean inverseMask = false;

    /** The value to return when the resulting object is empty. */
    @BeanField @Getter @Setter private double emptyValue = 255;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (!iterations.isAtLeastOnePositive()) {
            throw new BeanMisconfiguredException(
                    "At least one of iterationsDilation and iterationsErosion must be positive");
        }
    }

    @Override
    public String describeParameters() {
        return String.format(
                "%s,%s,inverse=%s",
                super.describeParameters(),
                iterations.describePropertiesFriendly(),
                inverse ? "true" : "false");
    }

    @Override
    protected double calculateForChannel(
            FeatureCalculationInput<FeatureInputSingleObject> input, Channel channel)
            throws FeatureCalculationException {

        ObjectMask objectShell = createShell(input);

        if (energyIndexMask != -1) {
            // If an Energy mask is defined...
            Optional<ObjectMask> omIntersected =
                    intersectWithEnergyMask(
                            objectShell, input.get().getEnergyStackRequired().withoutParameters());

            if (omIntersected.isPresent()) {
                objectShell = omIntersected.get();
            } else {
                return emptyValue;
            }
        }

        return calculateForShell(objectShell, channel);
    }

    /**
     * Calculates the feature value for the given shell and channel.
     *
     * @param shell the {@link ObjectMask} representing the shell
     * @param channel the {@link Channel} to calculate the feature on
     * @return the calculated feature value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double calculateForShell(ObjectMask shell, Channel channel)
            throws FeatureCalculationException;

    /**
     * Creates a shell object mask based on the input and configuration.
     *
     * @param input the feature calculation input
     * @return the created {@link ObjectMask} representing the shell
     * @throws FeatureCalculationException if the shell creation fails
     */
    private ObjectMask createShell(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {
        return input.calculate(
                CalculateShellObjectMask.of(input.resolver(), iterations, 0, inverse));
    }

    /**
     * Intersects the given object mask with the energy mask.
     *
     * @param object the {@link ObjectMask} to intersect
     * @param energyStack the {@link EnergyStackWithoutParameters} containing the energy mask
     * @return an {@link Optional} containing the intersected {@link ObjectMask}, or empty if the
     *     intersection is empty
     */
    private Optional<ObjectMask> intersectWithEnergyMask(
            ObjectMask object, EnergyStackWithoutParameters energyStack) {
        return object.intersect(createEnergyMask(energyStack), energyStack.extent());
    }

    /**
     * Creates an energy mask from the energy stack.
     *
     * @param energyStack the {@link EnergyStackWithoutParameters} to create the mask from
     * @return the created {@link ObjectMask} representing the energy mask
     */
    private ObjectMask createEnergyMask(EnergyStackWithoutParameters energyStack) {
        return new ObjectMask(
                new BoundingBox(energyStack.extent()),
                energyStack.getChannel(energyIndexMask).voxels().asByte(),
                inverseMask
                        ? BinaryValuesInt.getDefault().createInverted()
                        : BinaryValuesInt.getDefault());
    }
}
