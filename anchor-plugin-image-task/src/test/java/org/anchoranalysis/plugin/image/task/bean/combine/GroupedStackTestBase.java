/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.combine;

import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.io.input.bean.grouper.WithoutGrouping;
import org.anchoranalysis.plugin.image.task.bean.ColoredStacksInputFixture;
import org.anchoranalysis.plugin.image.task.bean.StackIOTestBase;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.anchoranalysis.plugin.io.bean.grouper.RemoveLastElement;
import org.anchoranalysis.test.experiment.task.ExecuteTaskHelper;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.Test;

/**
 * Base class for tests that read or write image-stacks.
 *
 * @author Owen Feehan
 */
abstract class GroupedStackTestBase extends StackIOTestBase {

    /**
     * Resizes the input images to a common size.
     *
     * <p>This means the input images have identical sizes.
     */
    @Test
    void testResize() throws OperationFailedException, ImageIOException {
        doTest(true, false, Optional.empty());
    }

    /**
     * Adds an additional three-channeled input with <i>unsigned-short</i> voxel-type, so the inputs
     * vary in voxel-type.
     */
    @Test
    void testMixedType() throws OperationFailedException, ImageIOException {
        assertThrows(
                OperationFailedException.class,
                () ->
                        doTest(
                                true,
                                false,
                                Optional.of(
                                        CreateVoxelsHelper.createStack(
                                                UnsignedShortVoxelType.INSTANCE, true))));
    }

    /**
     * Adds an additional single-channeled input, so the inputs do not all have the same number of
     * channels.
     */
    @Test
    void testMixedNumberChannels() throws OperationFailedException, ImageIOException {
        assertThrows(
                OperationFailedException.class,
                () ->
                        doTest(
                                true,
                                false,
                                Optional.of(
                                        new Stack(
                                                CreateVoxelsHelper.createChannel(
                                                        UnsignedByteVoxelType.INSTANCE)))));
    }

    /**
     * Adds an additional three-channeled input, but which has {@code rgb==false} unlike the other
     * inputs.
     */
    @Test
    void testMixedRGB() throws OperationFailedException, ImageIOException {
        assertThrows(
                OperationFailedException.class,
                () ->
                        doTest(
                                true,
                                false,
                                Optional.of(
                                        CreateVoxelsHelper.createStack(
                                                UnsignedByteVoxelType.INSTANCE, false))));
    }

    /** Resizes the input images to a common size, while also using <b>groups</b> */
    @Test
    void testGroups() throws OperationFailedException, ImageIOException {
        doTest(true, true, Optional.empty());
    }

    /**
     * Runs the test on the colored image inputs, and optionally an additional input.
     *
     * @param resizeTo when true, all images are resized to a common size. when false, their sizes
     *     remain unchanged.
     * @param groups when true, grouping is enabled.
     * @param additionalStack when set, adds an additional input based on {@link Stack}.
     * @throws OperationFailedException when thrown by {@link
     *     ExecuteTaskHelper#runTaskAndCompareOutputs(List,
     *     org.anchoranalysis.experiment.bean.task.Task, java.nio.file.Path, String, Iterable)}.
     * @throws ImageIOException when thrown by {@link ColoredStacksInputFixture#createInputs}.
     */
    protected void doTest(boolean resizeTo, boolean groups, Optional<Stack> additionalStack)
            throws OperationFailedException, ImageIOException {
        @SuppressWarnings("unchecked")
        List<ProvidesStackInput> inputs =
                (List<ProvidesStackInput>)
                        ColoredStacksInputFixture.createInputs(STACK_READER, true);

        if (additionalStack.isPresent()) {
            inputs.add(
                    new StackSequenceInputFixture(
                            additionalStack.get(), "someDir", "someFilename", Optional.empty()));
        }
        doTest(resizeTo, groups, inputs);
    }

    /**
     * Runs the test on particular inputs.
     *
     * @param resizeTo when true, all images are resized to a common size. when false, their sizes
     *     remain unchanged.
     * @param groups when true, grouping is enabled.
     * @param inputs the inputs for the task in the test.
     * @throws OperationFailedException when thrown by {@link
     *     ExecuteTaskHelper#runTaskAndCompareOutputs(List,
     *     org.anchoranalysis.experiment.bean.task.Task, java.nio.file.Path, String, Iterable)}.
     */
    protected void doTest(boolean resizeTo, boolean groups, List<ProvidesStackInput> inputs)
            throws OperationFailedException {

        GroupedStackBase<?, ?> task = createTaskWithOptions(resizeTo, groups);

        BeanInstanceMapFixture.check(task);

        ExecuteTaskHelper.runTaskAndCompareOutputs(
                inputs,
                task,
                directory,
                resizeTo ? subdirectoryResized() : subdirectoryNotResized(),
                filenamesToCompare(groups));
    }

    /**
     * Creates the task to be tested.
     *
     * <p>All necessary bean-fields should be assigned, apart from {@link
     * GroupedStackBase#setResizeTo(SizeXY)} which is set later.
     *
     * @return the task.
     */
    protected abstract GroupedStackBase<?, ?> createTask();

    /**
     * The names of the filenames to compare.
     *
     * @param groups whether the inputs are partitioned into groups or not.
     * @return the filenames.
     */
    protected abstract List<String> filenamesToCompare(boolean groups);

    /**
     * The name of subdirectory where the expected-output exists, when inputs are resized.
     *
     * @return the relative-path to the subdirectory, relative to the test root.
     */
    protected abstract String subdirectoryResized();

    /**
     * The name of subdirectory where the expected-output exists, when inputs are not resized.
     *
     * @return the relative-path to the subdirectory, relative to the test root.
     */
    protected abstract String subdirectoryNotResized();

    /** Creates the task to be tested, including setting group / resize options. */
    private GroupedStackBase<?, ?> createTaskWithOptions(boolean resizeTo, boolean groups) {
        GroupedStackBase<?, ?> task = createTask();
        task.setGroup(groups ? new RemoveLastElement() : new WithoutGrouping());
        task.setResizeTo(resizeTo ? new SizeXY(4, 6) : null);
        return task;
    }

    /** Creates a new list that adds a prefix to each item in an existing list. */
    protected static List<String> prependStrings(String prefix, List<String> list) {
        return FunctionalList.mapToList(list, element -> prefix + element);
    }
}
