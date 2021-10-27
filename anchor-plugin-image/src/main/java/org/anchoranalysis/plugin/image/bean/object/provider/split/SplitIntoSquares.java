/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.object.provider.split;

import java.util.Iterator;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Splits objects into sub-objects by cutting by an orthogonal square lattice (like a chessboard).
 *
 * <p>Only splits in x and y dimensions; the z-dimension is unaffected.
 *
 * <p>The cuts are mostly squares, but sometimes cuts are rectangles in the leftover space, which
 * are never larger in any dimension than {@code squareSize}
 *
 * <p>An optional minimim number of <i>on</i> voxels is applied to any object (after it has been cut) to
 * exist in the created collection.
 *
 * @author Owen Feehan
 */
public class SplitIntoSquares extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private int squareSize = 10;

    /** Only includes squares that have at least this number of voxels. */
    @BeanField @Getter @Setter private int minNumberVoxels = 1;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectCollection) {
        return objectCollection.stream().flatMap(this::splitObject);
    }

    private ObjectCollection splitObject(ObjectMask object) {

        Extent extent = object.boundingBox().extent();

        Iterator<BoundingBox> iterator = new SquareIterator(extent, squareSize, 1, extent.z());

        return ObjectCollectionFactory.mapFromOptional(
                iterator, box -> maybeExtractSquare(object, box));
    }

    private Optional<ObjectMask> maybeExtractSquare(ObjectMask objectToSplit, BoundingBox box) {

        ObjectMask square = extractFromObject(objectToSplit, box);

        // We only add the square if there's at least one voxel in it
        if (!square.voxelsOn().higherCountExistsThan(minNumberVoxels - 1)) {
            return Optional.empty();
        }

        return Optional.of(square);
    }

    private ObjectMask extractFromObject(ObjectMask objectToSplit, BoundingBox box) {

        // Voxels for the new square
        Voxels<UnsignedByteBuffer> voxelsNew = objectToSplit.voxels().extract().region(box, false);

        return new ObjectMask(
                box.shiftBy(objectToSplit.boundingBox().cornerMin()),
                voxelsNew,
                objectToSplit.binaryValuesByte());
    }
}
