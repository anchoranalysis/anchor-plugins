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

package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.nio.IntBuffer;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxelSliceBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedIntBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxels;

final class ConvertAllToConnectedComponents implements ProcessVoxelSliceBuffer<IntBuffer> {

    private final EncodedVoxels matS;
    private final Extent extent;

    /** A 3D offset for the 0th pixel in the current slice. */
    private int offsetZ = 0;

    public ConvertAllToConnectedComponents(EncodedVoxels matS) {
        super();
        this.matS = matS;
        this.extent = matS.extent();
    }

    @Override
    public void notifyChangeZ(int z) {
        offsetZ = extent.offset(0, 0, z);
    }

    @Override
    public void process(Point3i point, IntBuffer buffer, int offsetSlice) {
        assert (buffer != null);
        new EncodedIntBuffer(buffer, EncodedVoxels.ENCODING)
                .convertCode(offsetSlice, offsetZ + offsetSlice, matS, point);
    }
}
