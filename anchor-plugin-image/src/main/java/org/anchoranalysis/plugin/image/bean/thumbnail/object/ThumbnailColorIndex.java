package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.awt.Color;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.color.RGBColor;

/**
 * Creates a suitable color index for distinguishing between the different types of objects that
 * appear
 *
 * <p>The order of the objects is presented is important. First are the objects associated with the
 * input (one for single, two for pair). And the remainder of the objects are assumed to be other
 * objects shown in blue only for context.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class ThumbnailColorIndex implements ColorIndex {

    /** The color GREEN is used for the outline of object in the input (always) */
    private static final Color OUTLINE_FIRST_OBJECT = Color.GREEN;

    /**
     * The color RED is used for the outline of object of the second object in the input (if its
     * pairs)
     */
    private static final Color OUTLINE_SECOND_OBJECT = Color.RED;

    // START REQUIRED ARGUMENTS
    /**
     * Whether pairs are being used or not (in which case the second object is not treated as a
     * context object)
     */
    private final boolean pairs;

    /**
     * Color for outline of context objects (objects that aren't part of the input but which are
     * outlined as context)
     */
    private final Color colorContextObject;
    // END REQUIRED ARGUMENTS

    @Override
    public RGBColor get(int index) {
        return new RGBColor(colorForIndex(index));
    }

    @Override
    public boolean has(int index) {
        return true;
    }

    @Override
    public int numUniqueColors() {
        return 3;
    }

    private Color colorForIndex(int index) {
        if (index == 0) {
            return OUTLINE_FIRST_OBJECT;
        } else if (index == 1) {
            return pairs ? OUTLINE_SECOND_OBJECT : colorContextObject;
        } else {
            return colorContextObject;
        }
    }
}
