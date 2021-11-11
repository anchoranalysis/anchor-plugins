/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.math.relation.DoubleBiPredicate;

/**
 * If the XY resolution of an opened-image meets a certain condition then the resolution is scaled
 * by a factor.
 *
 * <p>This is useful for correcting situations where there has been a unit mixup by the {@link
 * StackReader}.
 *
 * <p>It assumes the X and Y resolution are equal. Throws an error otherwise.
 *
 * <p>If no image-resolution is known, an error will be thrown.
 *
 * @author Owen Feehan
 */
public class RejectIfConditionXYResolution extends StackReader {

    // START BEAN PROPERTIES
    /** Reads an image before a resolution is imposed. */
    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReader;

    /** A predicate defined by the relationship of the XY-resolution to a constant threshold. */
    @BeanField @Getter @Setter private RelationToThreshold relation;
    // END BEAN PROPERTIES

    @AllArgsConstructor
    private static class MaybeRejectProcessor
            implements OpenedRasterAlterDimensions.ConsiderUpdatedImageResolution {

        private DoubleBiPredicate relation;
        private double value;

        @Override
        public Optional<Resolution> maybeUpdatedResolution(Optional<Resolution> resolution)
                throws ImageIOException {

            if (!resolution.isPresent()) {
                throw new ImageIOException(
                        "No image-resolution is present, so cannot perform this check.");
            }

            if (!resolution.get().hasEqualXAndY()) {
                throw new ImageIOException("X and Y pixel-sizes are different. They must be equal");
            }

            if (relation.test(resolution.get().x(), value)) {
                throw new ImageIOException("XY-resolution fufills condition, and is thus rejected");
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public OpenedImageFile openFile(Path path, Logger logger) throws ImageIOException {
        OpenedImageFile openedFile = stackReader.openFile(path, logger);
        return new OpenedRasterAlterDimensions(
                openedFile, new MaybeRejectProcessor(relation.relation(), relation.threshold()));
    }
}
