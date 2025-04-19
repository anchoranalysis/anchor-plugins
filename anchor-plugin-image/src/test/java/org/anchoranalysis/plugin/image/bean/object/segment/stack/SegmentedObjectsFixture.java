/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.bean.object.segment.stack;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.time.ExecutionTimeRecorderIgnore;
import org.anchoranalysis.image.bean.displayer.IntensityQuantiles;
import org.anchoranalysis.image.bean.displayer.StackDisplayer;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.inference.segment.DualScale;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.image.inference.segment.MultiScaleObject;
import org.anchoranalysis.image.inference.segment.SegmentedBackground;
import org.anchoranalysis.image.inference.segment.SegmentedObjects;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point2d;
import org.anchoranalysis.test.image.object.CircleObjectFixture;
import org.anchoranalysis.test.image.object.CutOffCornersObjectFixture;

/**
 * Creates {@link SegmentedObjects} as used in tests.
 *
 * @author Owen Feehan
 */
public class SegmentedObjectsFixture {

    /** Class label for circle objects. */
    public static final String CLASS_LABEL_CIRCLE = "circle";

    /** Class label for cut-off corners objects. */
    public static final String CLASS_LABEL_CUT_OFF_CORNERS = "cut_off_corners";

    /** Number of circle objects to create. */
    private static final int NUMBER_CIRCLES = 7;

    /** Starting center point for circle objects. */
    private static final Point2d CIRCLE_START_CENTER = new Point2d(15, 15);

    /** Radius of circle objects. */
    private static final int CIRCLE_RADIUS = 5;

    /** Shift in center point between successive circle objects. */
    private static final Point2d CIRCLE_CENTER_SHIFT = new Point2d(3, 3);

    /** Shift in radius between successive circle objects. */
    private static final int CIRCLE_RADIUS_SHIFT = 1;

    /** Starting confidence value for objects. */
    private static final double CONFIDENCE_START = 0.2;

    /** Increment in confidence between successive objects. */
    private static final double CONFIDENCE_INCREMENT = 0.1;

    /** Background stack for segmented objects. */
    private static final Stack BACKGROUND = new Stack(new Extent(100, 200, 5));

    /** Stack displayer for segmented objects. */
    private static final StackDisplayer DISPLAYER = new IntensityQuantiles();

    /**
     * Creates a {@link SegmentedObjects} containing two possible class-labels and corresponding
     * objects.
     *
     * <p>The possible labels are {@link #CLASS_LABEL_CIRCLE} and {@link #CLASS_LABEL_CUT_OFF_CORNERS}.
     *
     * @param circles whether to include objects of type {@link #CLASS_LABEL_CIRCLE}.
     * @param cutOffCorners whether to include objects of type {@link #CLASS_LABEL_CUT_OFF_CORNERS}.
     * @return a newly created {@link SegmentedObjects} containing newly created objects of 0, 1 or
     *     2 class-types.
     */
    public static SegmentedObjects create(boolean circles, boolean cutOffCorners) {
        List<LabelledWithConfidence<MultiScaleObject>> list = new ArrayList<>();

        if (circles) {
            ObjectCollection circleObjects =
                    CircleObjectFixture.successiveCircles(
                            NUMBER_CIRCLES,
                            CIRCLE_START_CENTER,
                            CIRCLE_RADIUS,
                            CIRCLE_CENTER_SHIFT,
                            CIRCLE_RADIUS_SHIFT);
            addObjects(CLASS_LABEL_CIRCLE, circleObjects, list);
        }

        if (cutOffCorners) {
            ObjectCollection cutOffCornerObjects = new CutOffCornersObjectFixture().createAll();
            addObjects(CLASS_LABEL_CUT_OFF_CORNERS, cutOffCornerObjects, list);
        }

        SegmentedBackground background =
                new SegmentedBackground(new DualScale<>(BACKGROUND, BACKGROUND), DISPLAYER);

        return new SegmentedObjects(list, background, ExecutionTimeRecorderIgnore.instance());
    }

    /**
     * Adds objects to the list of {@link LabelledWithConfidence} objects.
     *
     * @param classLabel the class label for the objects
     * @param objects the {@link ObjectCollection} to add
     * @param list the list to add the objects to
     */
    private static void addObjects(
            String classLabel,
            ObjectCollection objects,
            List<LabelledWithConfidence<MultiScaleObject>> list) {
        // Add confidence successively with incrementing confidence
        List<LabelledWithConfidence<MultiScaleObject>> toAdd =
                FunctionalList.mapToListWithIndex(
                        objects.asList(),
                        (object, index) ->
                                new LabelledWithConfidence<>(
                                        new MultiScaleObject(() -> object, () -> object),
                                        (index * CONFIDENCE_INCREMENT) + CONFIDENCE_START,
                                        classLabel));
        list.addAll(toAdd);
    }
}