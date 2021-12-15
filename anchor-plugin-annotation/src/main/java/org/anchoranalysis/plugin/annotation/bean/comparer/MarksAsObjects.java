/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.ComparableSource;
import org.anchoranalysis.annotation.io.image.findable.Findable;
import org.anchoranalysis.annotation.io.image.findable.Found;
import org.anchoranalysis.annotation.io.image.findable.NotFound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.serialize.DeserializationFailedException;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.mpp.bean.regionmap.RegionMapSingleton;
import org.anchoranalysis.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.mpp.io.marks.MarkCollectionDeserializer;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;

/**
 * Loads a {@link MarkCollection} to compared, converting each {@link Mark} into an {@link
 * ObjectMask}.
 *
 * @author Owen Feehan
 */
public class MarksAsObjects extends ComparableSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DerivePath derivePath;
    // END BEAN PROPERTIES

    private static final RegionMembershipWithFlags REGION_MEMBERSHIP =
            RegionMapSingleton.instance()
                    .membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE);

    @Override
    public Findable<ObjectCollection> loadAsObjects(
            Path reference, Dimensions dimensions, boolean debugMode, OperationContext context)
            throws InputReadFailedException {

        Path path = path(reference);

        if (!path.toFile().exists()) {
            // There's nothing to annotate against
            return new NotFound<>(path, "No marks exist at path");
        }

        return new Found<>(createObjects(path, dimensions, context));
    }

    private Path path(Path filePathSource) throws InputReadFailedException {
        try {
            return derivePath.deriveFrom(filePathSource, false);
        } catch (DerivePathException e) {
            throw new InputReadFailedException(e);
        }
    }

    private ObjectCollection createObjects(
            Path filePath, Dimensions dimensions, OperationContext context)
            throws InputReadFailedException {
        MarkCollectionDeserializer deserialized = new MarkCollectionDeserializer();
        MarkCollection marks;
        try {
            marks = deserialized.deserialize(filePath, context);
        } catch (DeserializationFailedException e) {
            throw new InputReadFailedException(e);
        }

        return marks.deriveObjects(dimensions, REGION_MEMBERSHIP);
    }
}
