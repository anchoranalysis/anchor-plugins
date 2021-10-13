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

import java.util.Optional;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Remembers the different sizes among the files
 *
 * <p>It assumes numbering in ranges begins from 0 inclusive.
 *
 * @author Owen Feehan
 */
public class SizeExtents {

    private Optional<IntegerRange> rangeZ;
    private Optional<IntegerRange> rangeC;
    private Optional<IntegerRange> rangeT;

    private Integer sizeX = null;
    private Integer sizeY = null;

    // Assumes numbering starts from 0
    public SizeExtents(ParsedFilePathBag fileBag) {
        this.rangeZ = fileBag.rangeSliceNum();
        this.rangeC = fileBag.rangeChannelNum();
        this.rangeT = fileBag.rangeTimeIndex();
    }

    public boolean hasNecessaryExtents() {
        return rangeC.isPresent() && rangeZ.isPresent() && rangeT.isPresent();
    }

    public void populateMissingFromArbitrarySlice(Stack stackArbitrarySlice) {

        sizeX = stackArbitrarySlice.dimensions().x();
        sizeY = stackArbitrarySlice.dimensions().y();

        if (!rangeC.isPresent()) {
            rangeC = Optional.of(new IntegerRange(stackArbitrarySlice.getNumberChannels()));
        }

        if (!rangeZ.isPresent()) {
            rangeZ = Optional.of(new IntegerRange(stackArbitrarySlice.dimensions().z()));
        }

        if (!rangeT.isPresent()) {
            // If there's no indexes associated with the files, we assume there's a single index
            rangeT = Optional.of(new IntegerRange(1));
        }
    }

    public Extent toExtent() {
        return new Extent(sizeX, sizeY, rangeZ.map(IntegerRange::getSize).orElse(1));
    }

    public IntegerRange getRangeZ() {
        return rangeZ.get();
    }

    public IntegerRange getRangeC() {
        return rangeC.get();
    }

    public IntegerRange getRangeT() {
        return rangeT.get();
    }

    public boolean rangeTPresent() {
        return rangeT.isPresent();
    }

    public boolean rangeCPresent() {
        return rangeC.isPresent();
    }
}
