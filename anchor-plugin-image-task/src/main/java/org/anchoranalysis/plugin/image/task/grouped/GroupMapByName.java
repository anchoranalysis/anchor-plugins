/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.grouped;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.anchoranalysis.core.collection.MapCreate;
import org.anchoranalysis.core.collection.MapCreateCountdown;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedBiConsumer;
import org.anchoranalysis.core.functional.checked.CheckedConsumer;
import org.anchoranalysis.feature.io.name.MultiName;
import org.anchoranalysis.feature.io.name.MultiNameFactory;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.apache.commons.math3.util.Pair;

/**
 * Adds items to aggregate structures identified uniquely by a name, and allows these items to be
 * later outputted.
 *
 * <p>If any of the {@link MultiName} have a higher level aggregation-key, this is used to partition
 * the output at the end into sub-directories.
 *
 * @author Owen Feehan
 * @param <S> single item type
 * @param <T> aggregator type
 */
public abstract class GroupMapByName<S, T> {

    /**
     * A map of maps, indexing first by the <b>first part</b> of the {@link MultiName} and secondly
     * by the <b>second part</b>.
     */
    private final MapCreateCountdown<Optional<String>, MapCreate<String, T>> map;

    private final String nounT;

    /** Adds a single-item into an aggregator. */
    private final CheckedBiConsumer<S, T, OperationFailedException> addSingleToAggregator;

    /**
     * Creates a group-map.
     *
     * @param nounT a word to describe a single instance of T in user error messages.
     * @param groupIdentifiers a stream with each group-identifier that should be added to the map.
     * @param outputContext the subdirectory to output into. If not set, no outputs occur.
     * @param createAggregator called to create a new aggregator, whenever needed e.g. for a
     *     particular group.
     * @param addSingleToAggregator adds a single-item into an aggregator.
     */
    protected GroupMapByName(
            String nounT,
            Optional<Stream<String>> groupIdentifiers,
            Optional<InputOutputContext> outputContext,
            Supplier<T> createAggregator,
            CheckedBiConsumer<S, T, OperationFailedException> addSingleToAggregator) {
        this.map =
                new MapCreateCountdown<>(
                        () -> new MapCreate<>(createAggregator),
                        (groupIdentifier, groupMap) ->
                                outputGroup(groupIdentifier, groupMap, outputContext));

        if (groupIdentifiers.isPresent()) {
            // Increment the reference count for each instance of a group-identifier
            // This allows the map to already output each group, when all images have been processed
            // for that group
            groupIdentifiers.get().forEach(identifier -> map.increment(Optional.of(identifier)));
        } else {
            // Create one entry in the map for an empty-group. It's count will never be decremented.
            map.increment(Optional.empty());
        }

        this.nounT = nounT;
        this.addSingleToAggregator = addSingleToAggregator;
    }

    /**
     * Adds an item with a non-group identifier, and also optionally a group identifier.
     *
     * @param singleItemsToAdd the single-items to add, each with a corresponding non-group name.
     * @throws OperationFailedException if the operation cannot successfully complete.
     */
    public synchronized void add(
            Optional<String> groupIdentifier, List<Pair<String, S>> singleItemsToAdd)
            throws OperationFailedException {

        CheckedConsumer<MapCreate<String, T>, OperationFailedException> operation =
                value -> addAllItemsToMap(groupIdentifier, value, singleItemsToAdd);
        if (groupIdentifier.isPresent()) {
            map.processElementDecrement(groupIdentifier, operation);
        } else {
            map.processElement(groupIdentifier, operation);
        }
    }

    /**
     * Outputs any groups that have not already been outputted.
     *
     * @throws OperationFailedException if thrown by the outputting.
     */
    public void outputAnyRemainingGroups() throws OperationFailedException {
        map.cleanUpRemaining();
    }

    /**
     * Output a particular group into a subdirectory.
     *
     * @param namedAggregators all the aggregators for this group.
     * @param createContext the subdirectory into which outputting occurs, given a boolean which is
     *     true (when multiple outputs occur), or false (when a single output occurs).
     * @param outputNameSingle the output-name to use if there is only a single output, (in which
     *     case {@code createContext} should always be called with false).
     * @throws IOException if unable to output successfully.
     */
    protected abstract void outputGroupIntoSubdirectory(
            Collection<Map.Entry<String, T>> namedAggregators,
            Function<Boolean, InputOutputContext> createContext,
            Optional<String> outputNameSingle)
            throws IOException;

    /** Adds all the single-items to an aggregator retrieved from {@code map}. */
    private void addAllItemsToMap(
            Optional<String> groupIdentifier,
            MapCreate<String, T> map,
            List<Pair<String, S>> singleItemsToAdd)
            throws OperationFailedException {
        for (Pair<String, S> pair : singleItemsToAdd) {

            try {
                T aggregator = map.computeIfAbsent(pair.getFirst());
                addSingleToAggregator.accept(pair.getSecond(), aggregator);
            } catch (OperationFailedException e) {

                MultiName identifier = MultiNameFactory.create(groupIdentifier, pair.getFirst());
                throw new OperationFailedException(
                        String.format(
                                "An error occurred combining the %s created for: %s",
                                nounT, identifier),
                        e);
            }
        }
    }

    /**
     * Outputs data for a single "group" to the filesystem.
     *
     * @param groupIdentifier the identifier of the group to output (if it exists).
     * @param groupMap the corresponding map of elements for {@code groupIdentifier}.
     * @param context in which directory to perform the outputting.
     * @throws IOException if outputting doesn't occur successfully.
     */
    private void outputGroup(
            Optional<String> groupIdentifier,
            MapCreate<String, T> groupMap,
            Optional<InputOutputContext> outputContext)
            throws OperationFailedException {
        try {
            if (outputContext.isPresent()) {

                Set<Map.Entry<String, T>> entries = groupMap.entrySet();

                // If there is a second part-only in the MultiName, it is assumed that there is no
                // group (for all items) and it is written without a subdirectory
                // If there are two parts in the MultiName, it is assumed that the first-part is a
                // group-name (a separate subdirectory) and the second-part is written without a
                // subdirectory.
                outputGroupIntoSubdirectory(
                        entries,
                        multipleOutputs ->
                                maybeCreateSubdirectory(
                                        multipleOutputs, outputContext.get(), groupIdentifier),
                        groupIdentifier);
            }

        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Creates a subdirectory if grouping is occurring <b>and</b> multipleOutputs occur.
     *
     * @param multipleOutputs whether each group will produce multiple outputs or a single output.
     * @param context the context in which to maybe create a subdirectory, or else use as-is.
     * @param groupKey the key associated with the particular group.
     */
    private static InputOutputContext maybeCreateSubdirectory(
            boolean multipleOutputs, InputOutputContext context, Optional<String> groupKey) {
        if (groupKey.isPresent() && multipleOutputs) {
            return context.subdirectory(groupKey.get(), false);
        } else {
            return context;
        }
    }
}
