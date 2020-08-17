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

import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ObjectsWithBoundingBox;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;

/**
 * Exposes an iterable generator that accepts other kinds of objects as one that accepts a {@link
 * ObjectsWithBoundingBox}
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class WrapGenerators {

    /**
     * Gives an existing generator that accepts bounding-boxes an interface that accepts {@link
     * ObjectsWithBoundingBox}
     *
     * @param generator existing generator to wrap
     * @param flatten whether the bounding-box should be flattened in the z dimension
     * @return the wrapped generator
     */
    public static IterableGenerator<ObjectsWithBoundingBox> wrapBoundingBox(
            IterableGenerator<BoundingBox> generator, boolean flatten) {
        return IterableGeneratorBridge.createOneToMany(
                generator, objects -> Stream.of(boundingBoxFromObject(objects, flatten)));
    }

    /**
     * Gives an existing generator that accepts single-object-masks an interface that accepts {@link
     * ObjectsWithBoundingBox}
     *
     * @param generator existing generator to wrap
     * @param flatten whether the bounding-box should be flattened in the z dimension
     * @return the wrapped generator
     */
    public static IterableGenerator<ObjectsWithBoundingBox> wrapObjectMask(
            IterableGenerator<ObjectMask> generator) {
        return IterableGeneratorBridge.createOneToMany(
                generator, objects -> objects.objects().streamStandardJava());
    }

    private static BoundingBox boundingBoxFromObject(
            ObjectsWithBoundingBox objects, boolean flatten) {
        if (flatten) {
            return objects.boundingBox().flattenZ();
        } else {
            return objects.boundingBox();
        }
    }
}
