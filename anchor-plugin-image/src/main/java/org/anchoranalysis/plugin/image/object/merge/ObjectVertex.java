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

package org.anchoranalysis.plugin.image.object.merge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * A vertex in a merge graph representing an object (and and an associated payload)
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
public class ObjectVertex {

    // START REQUIRED ARGUMENTS
    @Getter private final ObjectMask object;
    private final double payload;
    // END REQUIRED ARGUMENTS

    /** Number of voxels in the object, calculated lazily */
    private int numberVoxels = -1;

    public double getPayload() {
        return payload;
    }

    public int numberVoxels() {
        if (numberVoxels == -1) {
            numberVoxels = object.numberVoxelsOn();
        }
        return numberVoxels;
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
