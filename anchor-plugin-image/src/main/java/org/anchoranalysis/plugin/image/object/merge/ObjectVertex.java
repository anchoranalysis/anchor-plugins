/* (C)2020 */
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
