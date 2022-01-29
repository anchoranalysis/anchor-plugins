package org.anchoranalysis.plugin.image.task.bean.combine;

import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
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
        doTest(true, Optional.empty());
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
                                Optional.of(createStack(UnsignedShortVoxelType.INSTANCE, true))));
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
                                Optional.of(
                                        new Stack(createChannel(UnsignedByteVoxelType.INSTANCE)))));
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
                                Optional.of(createStack(UnsignedByteVoxelType.INSTANCE, false))));
    }

    /**
     * Runs the test on the colored image inputs, and optionally an additional input.
     *
     * @param resizeTo when true, all images are resized to a common size. when false, their sizes
     *     remain unchanged.
     * @param additionalStack when set, adds an additional input based on {@link Stack}.
     * @throws OperationFailedException when thrown by {@link
     *     ExecuteTaskHelper#runTaskAndCompareOutputs(List,
     *     org.anchoranalysis.experiment.bean.task.Task, java.nio.file.Path, String, Iterable)}.
     * @throws ImageIOException when thrown by {@link ColoredStacksInputFixture#createInputs}.
     */
    protected void doTest(boolean resizeTo, Optional<Stack> additionalStack)
            throws OperationFailedException, ImageIOException {
        @SuppressWarnings("unchecked")
        List<ProvidesStackInput> inputs =
                (List<ProvidesStackInput>) ColoredStacksInputFixture.createInputs(STACK_READER);

        if (additionalStack.isPresent()) {
            inputs.add(
                    new StackSequenceInputFixture(
                            additionalStack.get(), "someDir", "someFilename"));
        }
        doTest(resizeTo, inputs);
    }

    /**
     * Runs the test on particular inputs.
     *
     * @param resizeTo when true, all images are resized to a common size. when false, their sizes
     *     remain unchanged.
     * @param inputs the inputs for the task in the test.
     * @throws OperationFailedException when thrown by {@link
     *     ExecuteTaskHelper#runTaskAndCompareOutputs(List,
     *     org.anchoranalysis.experiment.bean.task.Task, java.nio.file.Path, String, Iterable)}.
     */
    protected void doTest(boolean resizeTo, List<ProvidesStackInput> inputs)
            throws OperationFailedException {

        GroupedStackBase<?, ?> task = createTask();

        task.setResizeTo(resizeTo ? new SizeXY(4, 6) : null);

        BeanInstanceMapFixture.check(task);

        ExecuteTaskHelper.runTaskAndCompareOutputs(
                inputs,
                task,
                directory,
                resizeTo ? subdirectoryResized() : subdirectoryNotResized(),
                filenamesToCompare());
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
     * @return the filenames.
     */
    protected abstract Iterable<String> filenamesToCompare();

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

    /** Creates a three-channeled {@link Stack] with the same channel-names as the existing inputs. */
    private static Stack createStack(VoxelDataType voxelDataType, boolean rgb) {
        try {
            return new Stack(
                    rgb,
                    createChannel(voxelDataType),
                    createChannel(voxelDataType),
                    createChannel(voxelDataType));
        } catch (CreateException | IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    /** Creates a {@link Channel} with a particular data-type. */
    private static Channel createChannel(VoxelDataType voxelDataType) {
        return ChannelFactory.instance().create(new Dimensions(5, 6, 1), voxelDataType);
    }
}
