/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.image.bean.spatial.Padding;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.image.io.object.output.rgb.DrawCroppedObjectsGenerator;
import org.anchoranalysis.image.voxel.object.IntersectingObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.generator.collection.CollectionGenerator;
import org.anchoranalysis.io.generator.combined.CombinedListGenerator;
import org.anchoranalysis.io.output.enabled.single.SingleLevelOutputEnabled;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.overlay.bean.DrawObject;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;

/** @author Owen Feehan */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OutlineOutputHelper {

    /** A RGB stack with the outline of an object (and also another object if a pair) drawn. */
    public static final String OUTPUT_OUTLINE = "outline";

    /**
     * A XML file that encodes a point that uniquely lies on the object (and also another object if
     * a pair), assuming no objects overlap.
     */
    public static final String OUTPUT_UNIQUE_POINT_XML = "unique_point";

    /** Prefix before the pair images outputted. */
    private static final String PREFIX_OUTLINE = "outline";

    /** Outline color for the first object to be outlined. */
    private static final RGBColor COLOR_FIRST = new RGBColor(255, 0, 0);

    /** Outline color for the second object to be outlined (if it's a pair). */
    private static final RGBColor COLOR_SECOND = new RGBColor(0, 255, 0);

    /**
     * Is at least one of the outputs enabled?
     *
     * @param outputs rules for determine what outputs are enabled or not.
     * @return true if at least one output is enabled
     */
    public static boolean anyOutputEnabled(SingleLevelOutputEnabled outputs) {
        return outputs.isOutputEnabled(OUTPUT_OUTLINE)
                || outputs.isOutputEnabled(OUTPUT_UNIQUE_POINT_XML);
    }

    /**
     * Create a generator for a CSVRow that maybe draws an outline-RGB image and a unique-point XML
     *
     * @param objects all objects in the current image.
     * @param background a stack to display as a background
     * @param padding a margin around an object in each outline image
     * @param outputRules rules for determining which outputs are enabled or not
     * @return a newly created generator, containing only outputs that are enabled.
     * @throws OutputWriteFailedException if a background cannot be generated
     */
    public static CollectionGenerator<CSVRow> createGenerator(
            IntersectingObjects<ObjectMask> objects,
            CheckedSupplier<DisplayStack, OutputWriteFailedException> background,
            Padding padding,
            SingleLevelOutputEnabled outputRules)
            throws OutputWriteFailedException {

        ColorList colors = new ColorList(COLOR_FIRST, COLOR_SECOND);

        CombinedListGenerator<CSVRow> listGenerator = new CombinedListGenerator<>();

        if (outputRules.isOutputEnabled(OUTPUT_OUTLINE)) {

            CSVRowRGBOutlineGenerator outlineGenerator =
                    new CSVRowRGBOutlineGenerator(
                            croppedObjectsGenerator(background.get(), colors, padding), objects);

            listGenerator.add(OUTPUT_OUTLINE, outlineGenerator);
        }

        if (outputRules.isOutputEnabled(OUTPUT_UNIQUE_POINT_XML)) {
            listGenerator.add(OUTPUT_UNIQUE_POINT_XML, new CSVRowXMLGenerator());
        }

        // Output the group
        return new CollectionGenerator<>(listGenerator, PREFIX_OUTLINE);
    }

    private static DrawCroppedObjectsGenerator croppedObjectsGenerator(
            DisplayStack background, ColorList colors, Padding padding) {

        DrawObject drawer = new Outline(1, false);

        DrawCroppedObjectsGenerator delegate =
                new DrawCroppedObjectsGenerator(drawer, background, padding, colors);
        return delegate;
    }
}
