package org.anchoranalysis.plugin.image.task.slice;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.StackCopierAtBox;
import org.anchoranalysis.image.core.stack.RGBStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/**
 * A {@link Stack} into which individual images are written, together with the necessary information
 * on where to write each image.
 *
 * @author Owen Feehan
 */
public class MontageSharedState {

    private MontageLabels labels = new MontageLabels();

    /** The stack into which individual images are written. */
    @Getter private final RGBStack stack;

    /** The positions in {@code stack} for each respective input image. */
    private final Map<Path, BoundingBox> boxes;

    /** How to resize an image. */
    private final VoxelsResizer resizer;

    /**
     * Create with a mapping from {@link Path}s to boxes.
     *
     * @param boxes a mapping from each {@link Path} to the bounding-box in the combined {@link
     *     Stack}.
     * @param sizeCombined the size of the combined {@link Stack}.
     * @param resizer how to resize images.
     */
    public MontageSharedState(
            Map<Path, BoundingBox> boxes, Extent sizeCombined, VoxelsResizer resizer) {
        this.boxes = boxes;
        this.stack = new RGBStack(sizeCombined);
        this.resizer = resizer;
    }

    /**
     * Copies a {@link Stack} into a {@link BoundingBox} in the combined image, resizing if
     * necessary.
     *
     * <p>Any associated label is added to a queue, to be later drawn when {@link #drawAllLabels()}
     * is executed.
     *
     * @param source the image to copy from, not necessarily matching the final destination size. It
     *     is resized as necessary.
     * @param path the corresponding path to identify the appropriate {@link BoundingBox} to use in
     *     the combined image.
     * @param label if set, this label is drawn onto the bottom of the image. if not set, nothing
     *     occurs.
     * @throws OperationFailedException if no matching bounding-box exists.
     */
    public void copyStackInto(Stack source, Path path, Optional<String> label)
            throws OperationFailedException {

        BoundingBox box = boxes.get(path);

        if (box == null) {
            throw new OperationFailedException("Cannot find bounding-box to place " + path);
        }

        Stack sourceResized = source.mapChannel(channel -> channel.resizeXY(box.extent(), resizer));

        StackCopierAtBox.copyImageInto(sourceResized, stack.asStack(), box);

        if (label.isPresent()) {
            labels.add(label.get(), box);
        }
    }

    /**
     * Draw all labels that have been queued during calls to {@link #copyStackInto(Stack, Path,
     * Optional)}.
     *
     * <p>Once called, this class is no longer usable, and no subsequent methods should be called.
     *
     * @param ratioHeightForLabel how much of the average box height should the label approximately
     *     be sized to.
     * @throws OperationFailedException if the labels cannot be successfully drawn.
     */
    public void drawAllLabels(double ratioHeightForLabel) throws OperationFailedException {
        labels.flush(stack, ratioHeightForLabel);
    }
}
