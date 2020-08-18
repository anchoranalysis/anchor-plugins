package org.anchoranalysis.plugin.ij.mask;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.extent.Extent;
import ij.Prefs;
import ij.plugin.filter.Binary;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Applies an ImageJ (2D) morphological operation to voxels
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class ApplyImageJMorphologicalOperation {

    public static void fill(BinaryVoxels<ByteBuffer> voxels) throws OperationFailedException {
        applyOperation(voxels, "fill", 1);
    }
    
    public static BinaryVoxels<ByteBuffer> applyOperation(
            BinaryVoxels<ByteBuffer> voxels, String command, int iterations)
            throws OperationFailedException {

        if (!voxels.binaryValues().equals(BinaryValues.getDefault())) {
            throw new OperationFailedException("On byte must be 255, and off byte must be 0");
        }

        Prefs.blackBackground = true;

        Binary plugin = createPlugin(command, voxels.extent());

        for (int i = 0; i < iterations; i++) {
            applyOperation(plugin, voxels);
        }

        return voxels;
    }
    
    private static Binary createPlugin(String command, Extent extent) {
        Binary plugin = new Binary();
        plugin.setup(command, null);
        plugin.setNPasses(extent.z());
        return plugin;
    }
    
    private static void applyOperation(Binary plugin, BinaryVoxels<ByteBuffer> voxels) {
        // Are we missing a Z slice?
        voxels.extent().iterateOverZ( z-> 
            plugin.run( IJWrap.imageProcessorByte(voxels.slices(), z) )
        );
    }
}
