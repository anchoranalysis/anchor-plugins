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

import java.util.List;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import org.anchoranalysis.spatial.point.Point2d;
import org.anchoranalysis.test.image.object.CircleObjectFixture;
import org.anchoranalysis.test.image.object.CutOffCornersObjectFixture;

/**
 * Creates {@link SegmentedObjects} as used in tests.
 *
 * @author Owen Feehan
 */
public class SegmentedObjectsFixture {

    public static final String CLASS_LABEL_CIRCLE = "circle";

    public static final String CLASS_LABEL_CUT_OFF_CORNERS = "cut_off_corners";

    private static final int NUMBER_CIRCLES = 7;

    private static final Point2d CIRCLE_START_CENTER = new Point2d(15, 15);

    private static final int CIRCLE_RADIUS = 5;

    private static final Point2d CIRCLE_CENTER_SHIFT = new Point2d(8, 8);

    private static final int CIRCLE_RADIUS_SHIFT = 1;

    private static final double CONFIDENCE_START = 0.2;

    private static final double CONFIDENCE_INCREMENT = 0.1;

    /**
     * Creates a {@link SegmentedObjects} containing two possible class-labels and corresponding
     * objects.
     *
     * <p>The possible labels are {@link #CLASS_LABEL_CIRCLE} and.
     *
     * @param circles whether to include objects of type {@link #CLASS_LABEL_CIRCLE}.
     * @param cutOffCorners whether to include objects of type {@link #CLASS_LABEL_CUT_OFF_CORNERS}.
     * @return a newly created {@link SegmentedObjects} containing newly created objects of 0, 1 or
     *     2 class-types.
     */
    public static SegmentedObjects create(boolean circles, boolean cutOffCorners) {
        SegmentedObjects out = new SegmentedObjects();

        if (circles) {
            ObjectCollection circleObjects =
                    CircleObjectFixture.successiveCircles(
                            NUMBER_CIRCLES,
                            CIRCLE_START_CENTER,
                            CIRCLE_RADIUS,
                            CIRCLE_CENTER_SHIFT,
                            CIRCLE_RADIUS_SHIFT);
            addObjects(CLASS_LABEL_CIRCLE, circleObjects, out);
        }

        if (cutOffCorners) {
            ObjectCollection cutOffCornerObjects = new CutOffCornersObjectFixture().createAll();
            addObjects(CLASS_LABEL_CUT_OFF_CORNERS, cutOffCornerObjects, out);
        }

        return out;
    }

    private static void addObjects(
            String classLabel, ObjectCollection objects, SegmentedObjects out) {
        // Add confidence successively with incrementing confidence
        List<WithConfidence<ObjectMask>> list =
                FunctionalList.mapToListWithIndex(
                        objects.asList(),
                        (object, index) ->
                                new WithConfidence<>(
                                        object, (index * CONFIDENCE_INCREMENT) + CONFIDENCE_START));

        out.add(classLabel, list);
    }
}
