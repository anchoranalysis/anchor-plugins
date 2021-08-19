/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent;

import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.segment.optimization.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.optimization.kernel.KernelCalculationContext;

/**
 * Adds a new mark (a "birth") to create a proposal
 *
 * @author Owen Feehan
 * @param <T> proposal-type
 */
public abstract class KernelBirth<T> extends KernelPosNeg<T, UpdatableMarksList> {

    private Optional<Set<Mark>> setMarksNew = Optional.empty();

    // START BEAN PROPERTIES
    /** Total number of births */
    @BeanField @Getter @Setter private int repeats = 1;
    // END BEAN PROPERTIES

    @Override
    public Optional<T> makeProposal(Optional<T> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (!existing.isPresent()) {
            return Optional.empty();
        }

        setMarksNew = proposeNewMarks(existing.get(), repeats, context);
        return OptionalUtilities.flatMap(
                setMarksNew, set -> calculateForNewMark(existing.get(), set, context));
    }

    protected abstract Optional<Set<Mark>> proposeNewMarks(
            T exst, int number, KernelCalculationContext context);

    protected abstract Optional<T> calculateForNewMark(
            T exst, Set<Mark> listMarksNew, KernelCalculationContext context)
            throws KernelCalculateEnergyException;

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {

        double numerator = getProbNeg() * dimensions.calculateVolume() * poissonIntensity;
        double denominator = getProbPos() * proposalSize;

        assert (numerator > 0);
        if (denominator == 0) {
            return 1.0;
        }

        return Math.min((densityRatio * numerator) / denominator, 1.0);
    }

    @Override
    public String describeLast() {
        return String.format("birth_%d(%s)", repeats, idStr(setMarksNew.get())); // NOSONAR
    }

    @Override
    public int[] changedMarkIDArray() {
        return setMarksNew.map(KernelBirth::idArr).orElse(new int[] {});
    }

    protected Optional<Set<Mark>> marksNew() {
        return setMarksNew;
    }

    private static String idStr(Set<Mark> list) {
        return String.join(
                ", ",
                FunctionalList.mapToList(list, mark -> Integer.toString(mark.getIdentifier())));
    }

    private static int[] idArr(Set<Mark> set) {
        int[] arr = new int[set.size()];
        int i = 0;
        for (Mark m : set) {
            arr[i++] = m.getIdentifier();
        }
        return arr;
    }
}
