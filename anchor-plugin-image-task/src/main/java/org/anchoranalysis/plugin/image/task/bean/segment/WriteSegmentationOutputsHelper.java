package org.anchoranalysis.plugin.image.task.bean.segment;

import lombok.RequiredArgsConstructor;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.inference.segment.SegmentedObjects;
import org.anchoranalysis.image.inference.segment.SegmentedObjectsAtScale;
import org.anchoranalysis.image.io.object.output.grayscale.ObjectsMergedAsMaskGenerator;
import org.anchoranalysis.image.io.object.output.hdf5.HDF5ObjectsGenerator;
import org.anchoranalysis.image.io.object.output.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;

/**
 * Helpers write outputs related to segmentation.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class WriteSegmentationOutputsHelper {

    /** Output-name for the input-image for the segmentation. */
    static final String OUTPUT_INPUT_IMAGE = "input";

    /** Output-name for HDF5 encoded object-masks. */
    static final String OUTPUT_H5 = "objects";

    /** Output-name for object-masks merged together as a mask. */
    static final String OUTPUT_MERGED_AS_MASK = "mask";

    /** Output-name for a colored outline placed around the masks. */
    static final String OUTPUT_OUTLINE = "outline";

    /**
     * A suffix appended to some of the existing output-names, to generate an equivalent output but
     * but on a scaled version of the input image.
     *
     * <p>The scaled version corresponds to the image inputted to the model for inference.
     */
    static final String OUTPUT_NAME_SCALED_SUFFIX = "InputScale";

    // START REQUIRED ARGUMENTS
    /**
     * When true, the colors change for different objects in the image (using a default color set).
     *
     * <p>This takes precedence over {@code outlineColor}.
     */
    private final boolean varyColors;

    /** The width of the outline. */
    private final int outlineWidth;

    /** The color of the outline. */
    private final RGBColorBean outlineColor;
    // END REQUIRED ARGUMENTS

    public void writeOutputsForImage(
            Stack stack, SegmentedObjects segmentedObjects, Outputter outputter) {

        WriterRouterErrors writer = outputter.writerSelective();

        writer.write(OUTPUT_INPUT_IMAGE, () -> new StackGenerator(true, false), () -> stack);

        writer.write(
                OUTPUT_H5,
                HDF5ObjectsGenerator::new,
                segmentedObjects.getObjects().atInputScale()::objects);

        writeOuputsAtScale(
                writer, segmentedObjects.getObjects().atInputScale(), OUTPUT_NAME_SCALED_SUFFIX);
        writeOuputsAtScale(writer, segmentedObjects.getObjects().atModelScale(), "");
    }

    private void writeOuputsAtScale(
            WriterRouterErrors writer, SegmentedObjectsAtScale memoized, String outputNameSuffix) {
        writer.write(
                OUTPUT_MERGED_AS_MASK + outputNameSuffix,
                () -> new ObjectsMergedAsMaskGenerator(memoized.background().dimensions()),
                memoized::objects);

        writer.write(
                OUTPUT_OUTLINE + outputNameSuffix,
                () -> outlineGenerator(memoized.size(), memoized.backgroundDisplayStack()),
                memoized::objectsWithProperties);
    }

    private DrawObjectsGenerator outlineGenerator(int objectsSize, DisplayStack background) {
        if (varyColors) {
            return DrawObjectsGenerator.outlineVariedColors(objectsSize, outlineWidth, background);
        } else {
            return DrawObjectsGenerator.outlineSingleColor(
                    outlineWidth, background, outlineColor.toRGBColor());
        }
    }
}
