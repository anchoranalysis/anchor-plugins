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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
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
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.grouped.selectchannels.All;
import org.anchoranalysis.plugin.image.task.bean.grouped.selectchannels.FromStacks;
import org.anchoranalysis.plugin.image.task.channel.aggregator.NamedChannels;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;

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

    /**
     * If defined, translates a file-path into a group. If not-defined, all images are treated as
     * part of the same group
     */
    @BeanField @OptionalBean @Getter @Setter private DerivePath group;

    /** Selects which channels are included, optionally renaming. */
    @BeanField @Getter @Setter private FromStacks selectChannels = new All();

    /**
     * If set, each channel is scaled to a specific size before aggregation (useful for combining
     * different sized images)
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
        return new GroupedSharedState<>(
                checker ->
                        this.createGroupMap(checker, parameters.getContext().operationContext()));
    }

    @Override
    public void doJobOnInput(InputBound<ProvidesStackInput, GroupedSharedState<S, T>> input)
            throws JobExecutionException {

        ProvidesStackInput inputStack = input.getInput();
        InputOutputContext context = input.getContextJob();

        // Extract a group name
        Optional<String> groupName =
                extractGroupName(inputStack.pathForBinding(), context.isDebugEnabled());

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

        try {
            Optional<String> subdirectoryName = subdirectoryForGroupOutputs();
            if (context.getOutputter().outputsEnabled().isOutputEnabled(outputNameForGroups())) {
                sharedState
                        .getGroupMap()
                        .outputGroupedData(
                                sharedState.getChannelChecker(),
                                context.maybeSubdirectory(subdirectoryName, false));
            }

        } catch (IOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    /**
     * The first-level output-name used for determining if groups are written.
     *
     * <p>Second-level matches against this, will determine which specific groups may or may not be
     * written.
     *
     * @return
     */
    protected abstract String outputNameForGroups();

    /** An optional subdirectory where the group outputs are placed. */
    protected abstract Optional<String> subdirectoryForGroupOutputs();

    /**
     * Creates a map for the storing an aggregate-data-object for each group.
     *
     * @param channelChecker checks that the channels of all relevant stacks have the same size and
     *     data-type.
     * @param context supporting entities for the operation.
     * @return a newly created map.
     */
    protected abstract GroupMapByName<S, T> createGroupMap(
            ConsistentChannelChecker channelChecker, OperationContext context);

    /**
     * A function to derive the <i>individual</i> type used for aggregation from a {@link Channel}.
     *
     * @param source how to retrieve a {@link Channel}, appropriately-sized.
     * @return a function, that given a {@link Channel} will return an individual element of type
     *     {@code T}.
     */
    protected abstract CheckedFunction<Channel, S, CreateException> createChannelDeriver(
            ChannelSource source) throws OperationFailedException;

    /**
     * Processes each derived <i>individual</i> element from a {@link Channel}, calling {@code
     * consumeIndividual} one or more times.
     *
     * @param name the name of the channel.
     * @param individual the derived-individual element.
     * @param consumeIndividual a function that should be called one or more times for the
     *     individual element, or sub-elements of it.
     * @param context supporting entities for the operation.
     * @throws OperationFailedException if anything goes wrong during processing.
     */
    protected abstract void processIndividual(
            String name,
            S individual,
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

        ChannelSource source =
                new ChannelSource(
                        store,
                        sharedState.getChannelChecker(),
                        Optional.ofNullable(resizeTo),
                        getInterpolator().voxelsResizer());

        try {
            CheckedFunction<Channel, S, CreateException> deriveIndividualFromChannel =
                    createChannelDeriver(source);

            NamedChannels channels = getSelectChannels().selectChannels(source, true);

            sharedState
                    .getChannelNamesChecker()
                    .checkChannelNames(channels.names(), channels.isRgb());

            for (Map.Entry<String, Channel> entry : channels) {

                S individual = deriveIndividualFromChannel.apply(entry.getValue());

                processIndividual(
                        entry.getKey(),
                        individual,
                        (name, histogram) ->
                                sharedState.getGroupMap().add(groupName, name, individual),
                        context);
            }

        } catch (OperationFailedException | CreateException e) {
            throw new JobExecutionException(e);
        }
    }

    private Optional<String> extractGroupName(Optional<Path> path, boolean debugEnabled)
            throws JobExecutionException {

        // 	Return an arbitrary group-name if there's no binding-path, or a group-generator is not
        // defined
        if (group == null || !path.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(group.deriveFrom(path.get(), debugEnabled).toString());
        } catch (DerivePathException e) {
            throw new JobExecutionException(
                    String.format("Cannot establish a group-identifier for: %s", path), e);
        }
    }

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
