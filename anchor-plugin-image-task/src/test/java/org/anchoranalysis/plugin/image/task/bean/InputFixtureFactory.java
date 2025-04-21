/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2023 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean;

import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Creates {@link InputFixture}s that correspond to particular image directories in the test
 * resources.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InputFixtureFactory {

    /** The relative-path to the six colors. */
    private static final String PATH_SIX_COLORS = "montage/input/six";

    /** The relative-path to the six binary-mask files. */
    private static final String PATH_SIX_BINARY_MASKS = "scaleImageIndependently/input/sixBinary";

    /** The names of the files for the colored dataset (without extension). */
    private static final List<String> FILENAMES_SIX_COLORS =
            Arrays.asList("blue", "red", "yellow", "green", "gray", "orange");

    /** The names of the files for the colored dataset (without extension). */
    private static final List<String> FILENAMES_BINARY_MASKS =
            Arrays.asList("mask0", "mask1", "mask2", "mask3", "mask4", "mask5");

    /**
     * Create an fixture that returns six images, each with a different RGB color.
     *
     * @return a newly created fixture.
     */
    public static InputFixture createSixColors() {
        return new InputFixture(PATH_SIX_COLORS, FILENAMES_SIX_COLORS);
    }

    /**
     * Create an fixture that returns six binary-masks, each with a different rectangle present.
     *
     * @return a newly created fixture.
     */
    public static InputFixture createSixBinaryMasks() {
        return new InputFixture(PATH_SIX_BINARY_MASKS, FILENAMES_BINARY_MASKS);
    }
}
