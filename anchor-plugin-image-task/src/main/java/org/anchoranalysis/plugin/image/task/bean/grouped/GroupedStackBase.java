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

package org.anchoranalysis.plugin.image.task.bean.grouped;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.OptionalFactory;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.functional.checked.CheckedBiConsumer;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.nonbean.ConsistentChannelChecker;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.bean.grouper.Grouper;
import org.anchoranalysis.io.input.grouper.InputGrouper;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.grouped.selectchannels.All;
import org.anchoranalysis.plugin.image.task.bean.grouped.selectchannels.FromStacks;
import org.anchoranalysis.plugin.image.task.channel.aggregator.NamedChannels;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;
import org.apache.commons.math3.util.Pair;

/**
 * Base class for stacks (usually each channel from an image) that are somehow grouped-together.
 *
 * <p>Two types of entities are considered:
 *
 * <ul>
 *   <li>The <b>individual</b> type, to which a {@link Channel} is converted in the image.
 *   <li>The <b>aggregated</b> type, when multiple <i>individual</i> types are combined.
 * </ul>
 *
 * @author Owen Feehan
 * @param <S> individual-type
 * @param <T> aggregate-type
 */
public abstract class GroupedStackBase<S, T>
        extends Task<ProvidesStackInput, GroupedSharedState<S, T>> {

    // START BEAN PROPERTIES
    /** The interpolator to use for scaling images. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;

    /** How to partition the inputs into groups. */
    @BeanField @OptionalBean @Getter @Setter @DefaultInstance private Grouper group;

    /** Selects which channels are included, optionally renaming. */
    @BeanField @Getter @Setter private FromStacks selectChannels = new All();

    /**
     * If set, each channel is scaled to a specific size before aggregation (useful for combining
     * different sized images).
     */
    @BeanField @OptionalBean @Getter @Setter private SizeXY resizeTo;
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ProvidesStackInput.class);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public GroupedSharedState<S, T> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<ProvidesStackInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {

        // TODO
        // If grouping is enabled, create a map that counts the number of inputs for each group
        // This can then be used to reference-count the aggregate structure, so that it is outputted
        // automatically when the group is finished.

        OperationContext operationContext = parameters.getContext().operationContext();

        boolean outputEnabled =
                parameters
                        .getContext()
                        .getOutputter()
                        .outputsEnabled()
                        .isOutputEnabled(outputNameForGroups());
        Optional<InputOutputContext> outputContext =
                OptionalFactory.createChecked(
                        outputEnabled,
                        () ->
                                parameters
                                        .getContext()
                                        .maybeSubdirectory(subdirectoryForGroupOutputs(), false));

        Optional<InputGrouper> grouper =
                group.createInputGrouper(
                        parameters.getExecutionArguments().task().getGroupIndexRange());
        Optional<List<String>> groupIdentifiers =
                OptionalUtilities.map(
                        grouper, grouperInternal -> allGroupIdentifiers(inputs, grouperInternal));

        return new GroupedSharedState<>(
                grouper,
                checker ->
                        this.createGroupMap(
                                checker,
                                groupIdentifiers.map(List::stream),
                                outputContext,
                                operationContext));
    }

    @Override
    public void doJobOnInput(InputBound<ProvidesStackInput, GroupedSharedState<S, T>> input)
            throws JobExecutionException {

        ProvidesStackInput inputStack = input.getInput();
        InputOutputContext context = input.getContextJob();

        // Extract a group name
        Optional<String> groupName =
                deriveGroup(inputStack.identifierAsPath(), input.getSharedState().getGrouper());

        processStacks(
                GroupedStackBase.extractInputStacks(inputStack, context.getLogger()),
                groupName,
                input.getSharedState(),
                context);
    }

    @Override
    public void afterAllJobsAreExecuted(
            GroupedSharedState<S, T> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        // If no grouping was applied, it's now time to output the aggregate
        try {
            sharedState.getGroupMap().outputAnyRemainingGroups();
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException("An error occurred outputting an aggregate", e);
        }
    }

    /**
     * The first-level output-name used for determining if groups are written.
     *
     * <p>Second-level matches against this, and will determine which specific groups may or may not
     * be written.
     *
     * @return the output-name.
     */
    protected abstract String outputNameForGroups();

    /**
     * An optional subdirectory where the group outputs are placed.
     *
     * @return an {@link Optional} containing the subdirectory name as a {@link String}, or {@link
     *     Optional#empty()} if no subdirectory is specified.
     */
    protected abstract Optional<String> subdirectoryForGroupOutputs();

    /**
     * Creates a map for the storing an aggregate-data-object for each group.
     *
     * @param channelChecker checks that the channels of all relevant stacks have the same size and
     *     data-type.
     * @param groupIdentifiers a stream with each group-identifier that should be added to the map.
     * @param outputContext where to write results to when a group is processed.
     * @param operationContext supporting entities for the operation.
     * @return a newly created map.
     */
    protected abstract GroupMapByName<S, T> createGroupMap(
            ConsistentChannelChecker channelChecker,
            Optional<Stream<String>> groupIdentifiers,
            Optional<InputOutputContext> outputContext,
            OperationContext operationContext);

    /**
     * A function to derive the <i>individual</i> type used for aggregation from a {@link Channel}.
     *
     * @param source how to retrieve a {@link Channel}, appropriately-sized.
     * @return a function, that given a {@link Channel} will return an individual element of type
     *     {@code T}.
     * @throws OperationFailedException if the channel-deriver cannot be successfully created.
     */
    protected abstract CheckedFunction<Channel, S, CreateException> createChannelDeriver(
            ChannelSource source) throws OperationFailedException;

    /**
     * Processes each derived <i>individual</i> element from a {@link Channel}, calling {@code
     * consumeIndividual} one or more times.
     *
     * @param name the name of the channel.
     * @param individual the derived-individual element.
     * @param partOfGroup true when the item is part of a group, false otherwise.
     * @param consumeIndividual a function that should be called one or more times for the
     *     individual element, or sub-elements of it.
     * @param context supporting entities for the operation.
     * @throws OperationFailedException if anything goes wrong during processing.
     */
    protected abstract void processIndividual(
            String name,
            S individual,
            boolean partOfGroup,
            CheckedBiConsumer<String, S, OperationFailedException> consumeIndividual,
            InputOutputContext context)
            throws OperationFailedException;

    /**
     * Processes one set of named-stacks.
     *
     * @param stacks the named-stacks (usually each channel from an image).
     * @param groupName the name of the group.
     * @param sharedState shared-state.
     * @param context context for reading/writing.
     * @throws JobExecutionException if anything goes wrong.
     */
    private void processStacks(
            NamedStacks store,
            Optional<String> groupName,
            GroupedSharedState<S, T> sharedState,
            InputOutputContext context)
            throws JobExecutionException {

        // We collect every individual element to add to the aggregated in a list
        // so that it is one operation when they are added to the group map
        List<Pair<String, S>> toAdd = new LinkedList<>();

        try {
            ChannelSource source =
                    new ChannelSource(
                            store,
                            sharedState.getChannelChecker(),
                            Optional.ofNullable(resizeTo),
                            getInterpolator().voxelsResizer());

            CheckedFunction<Channel, S, CreateException> deriveIndividualFromChannel =
                    createChannelDeriver(source);

            NamedChannels channels = getSelectChannels().selectChannels(source, true);

            // Check that the channel-names are consistent across inputs.
            sharedState
                    .getChannelNamesChecker()
                    .checkChannelNames(channels.names(), channels.isRgb());

            for (Map.Entry<String, Channel> entry : channels) {

                S individual = deriveIndividualFromChannel.apply(entry.getValue());

                processIndividual(
                        entry.getKey(),
                        individual,
                        groupName.isPresent(),
                        (name, histogram) -> toAdd.add(new Pair<>(name, individual)),
                        context);
            }

        } catch (OperationFailedException | CreateException e) {

            // We always call this, even if there is nothing to add, so the group-map
            // can be reference counted appropriate
            addToGroupMap(sharedState, groupName, toAdd);

            throw new JobExecutionException(e);
        }

        // We always call this, even if there is nothing to add, so the group-map
        // can be reference counted appropriate
        addToGroupMap(sharedState, groupName, toAdd);
    }

    /** Adds {@code toAdd} to the group-map. */
    private void addToGroupMap(
            GroupedSharedState<S, T> sharedState,
            Optional<String> groupName,
            List<Pair<String, S>> toAdd)
            throws JobExecutionException {
        try {
            sharedState.getGroupMap().add(groupName, toAdd);
        } catch (OperationFailedException e) {
            throw new JobExecutionException("An error occurred updating the group map", e);
        }
    }

    /**
     * Derives a group-key for {@code identifier} or {@link Optional#empty} if grouping is disabled.
     */
    private Optional<String> deriveGroup(Path identifier, Optional<InputGrouper> grouper)
            throws JobExecutionException {
        try {
            if (!grouper.isPresent()) {
                return Optional.empty();
            }
            return Optional.of(grouper.get().deriveGroupKeyOptional(identifier));
        } catch (DerivePathException e) {
            throw new JobExecutionException(
                    String.format("Cannot establish a group-identifier for: %s", identifier), e);
        }
    }

    /** Extracts a group-identifier for every input. */
    private List<String> allGroupIdentifiers(List<ProvidesStackInput> inputs, InputGrouper grouper)
            throws ExperimentExecutionException {
        try {
            return FunctionalList.mapToList(
                    inputs,
                    DerivePathException.class,
                    input -> grouper.deriveGroupKeyOptional(input.identifierAsPath()));
        } catch (DerivePathException e) {
            throw new ExperimentExecutionException(
                    "Unable to derive a group identifier for an input", e);
        }
    }

    /** Creates a {@link NamedStacks} from a {@link ProvidesStackInput}. */
    private static NamedStacks extractInputStacks(ProvidesStackInput input, Logger logger)
            throws JobExecutionException {
        try {
            NamedStacks stacks = new NamedStacks();
            input.addToStoreInferNames(stacks, logger);
            return stacks;
        } catch (OperationFailedException e1) {
            throw new JobExecutionException("An error occurred creating inputs to the task", e1);
        }
    }
}
