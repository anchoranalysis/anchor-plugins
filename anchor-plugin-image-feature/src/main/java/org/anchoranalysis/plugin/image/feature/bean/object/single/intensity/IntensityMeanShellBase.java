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
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.morphological.MorphologicalIterations;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateShellObjectMask;

/**
 * Constructs a shell around an object-mask using a standard dilation and erosion process
 *
 * @author Owen Feehan
 */
public abstract class IntensityMeanShellBase extends FeatureNrgChnl {

    // START BEAN PROPERTIES
    /** The number of dilations and erosions to apply and whether to do in the Z dimension */
    @BeanField @Getter @Setter
    private MorphologicalIterations iterations = new MorphologicalIterations();

    /**
     * Iff TRUE, calculates instead on the inverse of the object-mask (what's left when the shell is
     * removed)
     */
    @BeanField @Getter @Setter private boolean inverse = false;

    /**
     * A channel of the nrgStack that is used as an additional mask using default byte values for ON
     * and OFF
     */
    @BeanField @Getter @Setter private int nrgIndexMask = -1;

    @BeanField @Getter @Setter
    private boolean inverseMask = false; // Uses the inverse of the passed mask

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
    protected double calculateForChannel(SessionInput<FeatureInputSingleObject> input, Channel chnl)
            throws FeatureCalculationException {

        ObjectMask objectShell = createShell(input);

        if (nrgIndexMask != -1) {
            // If an NRG mask is defined...
            Optional<ObjectMask> omIntersected =
                    intersectWithNRGMask(
                            objectShell, input.get().getNrgStackRequired().getNrgStack());

            if (omIntersected.isPresent()) {
                objectShell = omIntersected.get();
            } else {
                return emptyValue;
            }
        }

        return calculateForShell(objectShell, chnl);
    }

    private ObjectMask createShell(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {
        return input.calc(CalculateShellObjectMask.of(input.resolver(), iterations, 0, inverse));
    }

    private Optional<ObjectMask> intersectWithNRGMask(ObjectMask object, NRGStack nrgStack) {
        return object.intersect(createNrgMask(nrgStack), nrgStack.dimensions());
    }

    protected abstract double calculateForShell(ObjectMask shell, Channel chnl)
            throws FeatureCalculationException;

    private ObjectMask createNrgMask(NRGStack nrgStack) {
        return new ObjectMask(
                new BoundingBox(nrgStack.dimensions()),
                nrgStack.getChannel(nrgIndexMask).voxels().asByte(),
                inverseMask
                        ? BinaryValues.getDefault().createInverted()
                        : BinaryValues.getDefault());
    }

    @Override
    public String describeParams() {
        return String.format(
                "%s,%s,inverse=%s",
                super.describeParams(),
                iterations.describePropertiesFriendly(),
                inverse ? "true" : "false");
    }
}
