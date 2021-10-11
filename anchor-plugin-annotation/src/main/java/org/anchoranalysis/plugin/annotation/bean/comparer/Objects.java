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
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.io.object.input.ObjectCollectionReader;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;

/**
 * An {@link ObjectCollection} to be compared against something else.
 *
 * @author Owen Feehan
 */
public class Objects extends ComparableSource {

    // START BEAN PROPERTIES
    /** Where to find the objects to compare, as a function of the source file-path. */
    @BeanField @Getter @Setter private DerivePath derivePath;
    // END BEAN PROPERTIES

    @Override
    public Findable<ObjectCollection> loadAsObjects(
            Path filePathSource, Dimensions dimensions, boolean debugMode)
            throws InputReadFailedException {

        try {
            Path objectsPath = derivePath.deriveFrom(filePathSource, debugMode);

            if (!objectsPath.toFile().exists()) {
                return new NotFound<>(objectsPath, "No objects exist");
            }

            return new Found<>(ObjectCollectionReader.createFromPath(objectsPath));

        } catch (DerivePathException | DeserializationFailedException e) {
            throw new InputReadFailedException(e);
        }
    }
}