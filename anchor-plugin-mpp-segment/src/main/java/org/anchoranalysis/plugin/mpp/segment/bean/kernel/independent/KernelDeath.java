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
import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;

public abstract class KernelDeath<T> extends KernelPosNeg<T> {

    private Optional<Mark> markRmv = Optional.empty();

    @Override
    public Optional<T> makeProposal(Optional<T> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (!existing.isPresent()) {
            return Optional.empty();
        }

        Optional<MarkAnd<Mark, T>> markEnergy =
                removeAndUpdateEnergy(existing.get(), context.proposer());
        markRmv = markEnergy.map(MarkAnd::getMark);
        return markEnergy.map(MarkAnd::getCollection);
    }

    protected abstract Optional<MarkAnd<Mark, T>> removeAndUpdateEnergy(
            T exst, ProposerContext context) throws KernelCalculateEnergyException;

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {

        if (existingSize <= 1) {
            return Math.min(1.0, densityRatio);
        }

        // Birth prob
        double num = getProbNeg() * existingSize;

        // Death prob
        double dem = getProbPos() * dimensions.calculateVolume() * poissonIntensity;

        return Math.min(1.0, densityRatio * num / dem);
    }

    @Override
    public String describeLast() {
        if (markRmv.isPresent()) {
            return String.format("death(%d)", markRmv.get().getId());
        } else {
            return "death";
        }
    }

    @Override
    public int[] changedMarkIDArray() {
        if (markRmv.isPresent()) {
            return new int[] {this.markRmv.get().getId()};
        } else {
            return new int[] {};
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    protected Optional<Mark> getMarkRmv() {
        return markRmv;
    }

    protected static int selectIndexToRmv(MarkCollection exst, ProposerContext propContext) {

        if (exst.size() == 0) {
            propContext.getErrorNode().add("configuration size is 0");
            return -1;
        }

        // Random mark
        return exst.randomIndex(propContext.getRandomNumberGenerator());
    }

    @AllArgsConstructor
    @Value
    protected static class MarkAnd<S, T> {
        private S mark;
        private T collection;
    }
}
