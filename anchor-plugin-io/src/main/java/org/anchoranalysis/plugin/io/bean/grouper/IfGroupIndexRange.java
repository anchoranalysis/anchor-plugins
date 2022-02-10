/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.grouper;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.range.IndexRangeNegative;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.bean.grouper.Grouper;
import org.anchoranalysis.io.input.bean.grouper.WithoutGrouping;
import org.anchoranalysis.io.input.grouper.InputGrouper;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.input.path.ExtractPathElementRange;

/**
 * When a {@link IndexRangeNegative} is passed as an argument, an {@link InputGrouper} is
 * constructed that extracts elements from a {@link Path}.
 *
 * <p>See {@link ExtractPathElementRange#extract} for how this occurs.
 *
 * <p>Otherwise, {@code group} is called to create a {@code Optional<InputGrouper>}.
 *
 * @author Owen Feehan
 */
public class IfGroupIndexRange extends Grouper {

    // START BEAN PROPERTIES
    /** Fallback to use when no group-index-range is specified. */
    @BeanField @Getter @Setter private Grouper group = new WithoutGrouping();
    // END BEAN PROPERTIES

    @Override
    public Optional<InputGrouper> createInputGrouper(Optional<IndexRangeNegative> groupIndexRange) {
        if (groupIndexRange.isPresent()) {
            return Optional.of(identifier -> extractSubrange(identifier, groupIndexRange.get()));
        } else {
            return group.createInputGrouper(groupIndexRange);
        }
    }

    /** Extracts a range of elements from {@code identifier}. */
    private static String extractSubrange(Path identifier, IndexRangeNegative groupIndexRange)
            throws DerivePathException {
        Path extractedPath = ExtractPathElementRange.extract(identifier, groupIndexRange);
        return FilePathToUnixStyleConverter.toStringUnixStyle(extractedPath);
    }
}
