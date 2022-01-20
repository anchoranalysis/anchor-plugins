package org.anchoranalysis.plugin.image.task.slice;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.BoundingBoxEnclosed;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.StackCopierAtBox;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.image.core.stack.RGBStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/**
 * A {@link Stack} into which individual images are written, together with the necessary information
 * on where to write each image.
 *
 * @author Owen Feehan
 */
public class MontageSharedState {

    private final MontageLabels labels;

    /** The stack into which individual images are written. */
    @Getter private final RGBStack stack;

    /** The positions in {@code stack} for each respective input image. */
    private final Map<Path, BoundingBoxEnclosed> boxes;

    /** How to resize an image. */
    private final VoxelsResizer resizer;

    /**
     * Create with a mapping from {@link Path}s to boxes.
     *
     * @param boxes a mapping from each {@link Path} to the bounding-box in the combined {@link
     *     Stack}.
     * @param sizeCombined the size of the combined {@link Stack}.
     * @param resizer how to resize images.
     * @param executionTimeRecorder records the execution time of certain operations.
     */
    public MontageSharedState(
            Map<Path, BoundingBoxEnclosed> boxes,
            Extent sizeCombined,
            VoxelsResizer resizer,
            ExecutionTimeRecorder executionTimeRecorder) {
        this.boxes = boxes;
        this.stack = new RGBStack(sizeCombined);
        this.resizer = resizer;
        this.labels = new MontageLabels(executionTimeRecorder);
    }

    /**
     * Copies a {@link Stack} into a {@link BoundingBox} in the combined image, resizing if
     * necessary.
     *
     * <p>Any associated label is added to a queue, to be later drawn when {@link #drawAllLabels} is
     * executed.
     *
     * <p>The {@link Stack} is read only lazily, to try and prevent errors until {@link
     * BoundingBoxEnclosed} is retrieved, so that a label can always be written, even in the event
     * of copying failure.
     *
     * @param source supplies the image to copy from, not necessarily matching the final destination
     *     size. It is resized as necessary.
     * @param path the corresponding path to identify the appropriate {@link BoundingBox} to use in
     *     the combined image.
     * @param label if set, this label is drawn onto the bottom of the image. if not set, nothing
     *     occurs.
     * @throws OperationFailedException if no matching bounding-box exists, or the input cannot be
     *     successfully read, or copying otherwise fails.
     */
    public void copyStackInto(
            CheckedSupplier<Stack, InputReadFailedException> source,
            Path path,
            Optional<String> label)
            throws OperationFailedException {

        BoundingBoxEnclosed box = boxes.get(path);

        if (box == null) {
            throw new OperationFailedException(
                    "Cannot find bounding-box to place image `" + path + "` into the montage.");
        }

        try {
            Stack sourceResized =
                    source.get()
                            .mapChannel(
                                    channel -> channel.resizeXY(box.getBox().extent(), resizer));

            StackCopierAtBox.copyImageInto(sourceResized, stack.asStack(), box.getBox());

            if (label.isPresent()) {
                labels.add(label.get(), box.getEnclosing(), false);
            }

        } catch (InputReadFailedException e) {

            if (label.isPresent()) {
                labels.add(label.get(), box.getEnclosing(), true);
            }

            throw new OperationFailedException(e);
        }
    }

    /**
     * Draw all labels that have been queued during calls to {@link #copyStackInto}.
     *
     * <p>Once called, this class is no longer usable, and no subsequent methods should be called.
     *
     * @param ratioHeightForLabel how much of the average box height should the label approximately
     *     be sized to.
     * @param aligner how to align the label on its respective associated image.
     * @throws OperationFailedException if the labels cannot be successfully drawn.
     */
    public void drawAllLabels(double ratioHeightForLabel, BoxAligner aligner)
            throws OperationFailedException {
        labels.flush(stack, ratioHeightForLabel, aligner);
    }
}
