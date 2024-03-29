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

package org.anchoranalysis.plugin.io.multifile;

import java.util.Iterator;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class SortedSetChannel implements Comparable<SortedSetChannel>, Iterable<SortedSetSlice> {

    @Getter private int channelNum;

    private TreeMap<Integer, SortedSetSlice> slices = new TreeMap<>();

    public int numSlices() {
        return this.slices.size();
    }

    public void add(int sliceNum, String filePath) {

        SortedSetSlice slice = slices.get(sliceNum);

        if (slice == null) {
            slice = new SortedSetSlice(filePath, sliceNum);
            slices.put(sliceNum, slice);
        } else {
            slice.setFilePath(filePath);

            // We shouldn't ever have two channels with the same ID
            assert false;
        }
    }

    @Override
    public int compareTo(SortedSetChannel arg0) {

        if (channelNum == arg0.channelNum) {
            return 0;
        } else if (channelNum < arg0.channelNum) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public Iterator<SortedSetSlice> iterator() {
        return slices.values().iterator();
    }
}
