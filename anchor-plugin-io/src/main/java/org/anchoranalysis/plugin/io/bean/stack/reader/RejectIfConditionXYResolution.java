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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.stack.OpenedRaster;

// If the XY resolution of an opened-image meets a certain condition
//  then the resolution is scaled by a factor
//
// This is useful for correcting situations where there has been a unit
//  mixup by the reader
//
// Assumes the X and Y resolution are equal. Throws an error otherwise.
public class RejectIfConditionXYResolution extends StackReader {

    // START BEAN PROPERTIES
    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReader;

    @BeanField @Getter @Setter private RelationBean relation;

    @BeanField @NonNegative @Getter @Setter private double value;
    // END BEAN PROPERTIES

    private static class MaybeRejectProcessor
            implements OpenedRasterAlterDimensions.ConsiderUpdatedImageResolution {

        private RelationToValue relation;
        private double value;

        public MaybeRejectProcessor(RelationToValue relation, double value) {
            super();
            this.relation = relation;
            this.value = value;
        }

        @Override
        public Optional<Resolution> maybeUpdatedResolution(Resolution resolution)
                throws RasterIOException {

            if (resolution.x() != resolution.y()) {
                throw new RasterIOException(
                        "X and Y pixel-sizes are different. They must be equal");
            }

            if (relation.isRelationToValueTrue(resolution.x(), value)) {
                throw new RasterIOException(
                        "XY-resolution fufills condition, and is thus rejected");
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public OpenedRaster openFile(Path path) throws RasterIOException {
        OpenedRaster or = stackReader.openFile(path);
        return new OpenedRasterAlterDimensions(
                or, new MaybeRejectProcessor(relation.create(), value));
    }
}
