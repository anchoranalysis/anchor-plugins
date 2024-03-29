/*-
 * #%L
 * anchor-plugin-image-task
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
package org.anchoranalysis.plugin.image.task.size;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Maps a {@link Path} to a particular image-size and orientation-change.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class SizeMapping {

    /** The path. */
    @Getter private Path path;

    /** The size. */
    @Getter private Extent extent;

    /**
     * Replace the existing {@link Extent} with a new value.
     *
     * @param extent the extent to assign.
     */
    public void assignExtent(Extent extent) {
        this.extent = extent;
    }

    /**
     * Scales the XY dimensions the factor.
     *
     * <p>This is an immutable method.
     *
     * @param scaleFactor the factor to scale by.
     * @return a new {@link SizeMapping} with the scale-factor applied.
     */
    public SizeMapping scaleXYBy(ScaleFactor scaleFactor) {
        return new SizeMapping(path, extent.scaleXYBy(scaleFactor, true));
    }
}
