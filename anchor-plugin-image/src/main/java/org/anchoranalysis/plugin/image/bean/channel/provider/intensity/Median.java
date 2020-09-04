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

package org.anchoranalysis.plugin.image.bean.channel.provider.intensity;

import com.google.common.collect.BoundType;
import com.google.common.collect.TreeMultiset;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import java.util.Iterator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.PrimitiveConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.Voxels;

/**
 * Applies a median-filter with square kernel
 *
 * @author Owen Feehan
 */
public class Median extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    /**
     * Kernel will be a square kernel of size {@code (2*kernelHalfWidth+1)x(2*kernelHalfWidth+1)}
     */
    @BeanField @Getter @Setter private int kernelHalfWidth;
    // END BEAN PROPERTIES

    @RequiredArgsConstructor
    private static class RollingMultiSet {

        // START REQUIRED ARGUMENTS
        private final int kernelHalfWidth;
        // END REQUIRED ARGUMENTS

        private TreeMultiset<Integer> set = TreeMultiset.create();

        public void clear() {
            set.clear();
        }

        public void populateAt(int xCenter, int yMin, int yMax, UnsignedByteBuffer bb, Extent extent) {

            int xMin = xCenter - kernelHalfWidth;
            int xMax = xCenter + kernelHalfWidth;

            xMin = Math.max(xMin, 0);
            xMax = Math.min(xMax, extent.x() - 1);

            for (int y = yMin; y <= yMax; y++) {
                for (int x = xMin; x <= xMax; x++) {

                    int val = PrimitiveConverter.unsignedByteToInt(bb.get(extent.offset(x, y)));
                    set.add(val);
                }
            }
        }

        public void removeColumn(int x, int yMin, int yMax, UnsignedByteBuffer bb, Extent e) {

            if (x < 0) {
                return;
            }
            if (x >= e.x()) {
                return;
            }

            for (int y = yMin; y <= yMax; y++) {
                int val = PrimitiveConverter.unsignedByteToInt(bb.get(e.offset(x, y)));
                assert (set.contains(val));
                set.remove(val);
            }
        }

        public void addColumn(int x, int yMin, int yMax, UnsignedByteBuffer bb, Extent e) {

            if (x < 0) {
                return;
            }
            if (x >= e.x()) {
                return;
            }

            for (int y = yMin; y <= yMax; y++) {
                int val = PrimitiveConverter.unsignedByteToInt(bb.get(e.offset(x, y)));
                set.add(val);
            }
        }

        public int median() {
            int size = size();
            int medianIndex = size / 2;

            Iterator<Integer> itr = set.headMultiset(255, BoundType.CLOSED).iterator();

            for (int i = 1; i < medianIndex; i++) {
                itr.next();
            }

            return itr.next();
        }

        public int size() {
            return set.headMultiset(255, BoundType.CLOSED).size();
        }
    }

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {

        Voxels<UnsignedByteBuffer> voxels = channel.voxels().asByte();

        RollingMultiSet set = new RollingMultiSet(kernelHalfWidth);

        Channel dup = channel.duplicate();
        Voxels<UnsignedByteBuffer> voxelsDup = dup.voxels().asByte();
        Extent extent = dup.extent();

        for (int z = 0; z < extent.z(); z++) {

            UnsignedByteBuffer buffer = voxels.sliceBuffer(z);
            UnsignedByteBuffer bufferOut = voxelsDup.sliceBuffer(z);

            int offset = 0;
            for (int y = 0; y < extent.y(); y++) {

                int yMin = y - kernelHalfWidth;
                int yMax = y + kernelHalfWidth;

                yMin = Math.max(yMin, 0);
                yMax = Math.min(yMax, extent.y() - 1);

                for (int x = 0; x < extent.x(); x++) {

                    if (x == 0) {
                        set.clear();
                        set.populateAt(x, yMin, yMax, buffer, extent);
                    } else {
                        set.removeColumn(x - kernelHalfWidth - 1, yMin, yMax, buffer, extent);
                        set.addColumn(x + kernelHalfWidth, yMin, yMax, buffer, extent);
                    }

                    int median = set.median();

                    bufferOut.put(offset, (byte) median);
                    offset++;
                }
            }
        }
        return dup;
    }
}
