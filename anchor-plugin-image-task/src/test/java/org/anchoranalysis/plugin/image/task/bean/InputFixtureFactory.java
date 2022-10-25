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
    private static String PATH_SIX_COLORS = "montage/input/six";

    /** The relative-path to the six binary-mask files. */
    private static String PATH_SIX_BINARY_MASKS = "scaleImageIndependently/input/sixBinary";

    /** The names of the files for the colored dataset (without extension). */
    private static List<String> FILENAMES_SIX_COLORS =
            Arrays.asList("blue", "red", "yellow", "green", "gray", "orange");

    /** The names of the files for the colored dataset (without extension). */
    private static List<String> FILENAMES_BINARY_MASKS =
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
