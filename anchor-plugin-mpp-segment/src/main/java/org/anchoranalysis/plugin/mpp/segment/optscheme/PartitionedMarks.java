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

package org.anchoranalysis.plugin.mpp.segment.optscheme;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

/**
 * Binary partition of marks into "available" and "accepted"
 *
 * <p>Items in the "available" partition can be sampled (with varying weights for items).
 *
 * @author Owen Feehan
 */
public class PartitionedMarks {

    private final Set<Mark> available;
    private final Set<Mark> accepted;
    private final ToDoubleFunction<Mark> funcExtractWeight;
    private Optional<EnumeratedDistribution<Mark>> distance;

    /**
     * Constructor
     *
     * @param marks configuration to partition
     * @param funcExtractWeight sampling-weight for a given item
     */
    public PartitionedMarks(MarkCollection marks, ToDoubleFunction<Mark> funcExtractWeight) {
        this.available = new HashSet<>(marks.getMarks());
        this.accepted = new HashSet<>();
        this.funcExtractWeight = funcExtractWeight;
        distance = createSamplingDistribution();
    }

    /**
     * Samples from the available marks
     *
     * @param idealNumItems number of items to be sampled (if they are available, otherwise as many
     *     as available are returned)
     * @return if marks were sampled
     */
    public Optional<Set<Mark>> sampleFromAvailable(int idealNumItems) {
        return distance.map(
                d -> {
                    int numToSample = Math.min(idealNumItems, available.size());

                    Mark[] arr = new Mark[numToSample];
                    d.sample(numToSample, arr);
                    return convertArrToSet(arr);
                });
    }

    public void moveAvailableToAccepted(Set<Mark> list) {
        for (Mark item : list) {
            moveAvailableToAccepted(item);
        }
        distance = createSamplingDistribution();
    }

    @Override
    public String toString() {
        return String.format("available=%d,accepted=%d", available.size(), accepted.size());
    }

    private void moveAvailableToAccepted(Mark pxlMark) {
        available.remove(pxlMark);
        accepted.add(pxlMark);
    }

    public void moveAcceptedToAvailable(Mark pxlMark) {
        accepted.remove(pxlMark);
        available.add(pxlMark);
        distance = createSamplingDistribution();
    }

    private Optional<EnumeratedDistribution<Mark>> createSamplingDistribution() {

        // Early exit when nothing is available
        if (available.isEmpty()) {
            return Optional.empty();
        }

        List<Pair<Mark, Double>> list =
                FunctionalList.mapToList(
                        available, item -> new Pair<>(item, funcExtractWeight.applyAsDouble(item)));
        return Optional.of(new EnumeratedDistribution<>(list));
    }

    private static Set<Mark> convertArrToSet(Mark[] arr) {
        Set<Mark> set = new HashSet<>();
        CollectionUtils.addAll(set, arr);
        return set;
    }
}
