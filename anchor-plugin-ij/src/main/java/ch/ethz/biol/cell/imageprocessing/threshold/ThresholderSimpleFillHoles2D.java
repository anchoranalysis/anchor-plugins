/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.threshold;

import ij.Prefs;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;
import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.Thresholder;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.thresholder.VoxelBoxThresholder;

@NoArgsConstructor
@AllArgsConstructor
public class ThresholderSimpleFillHoles2D extends Thresholder {

    static {
        Prefs.blackBackground = true;
    }

    // START BEAN PROPERTIES
    /** Intensity for thresholding */
    @BeanField @Getter @Setter private int minIntensity = -1;
    // END BEAN PROPERTIES

    @Override
    public BinaryVoxelBox<ByteBuffer> threshold(
            VoxelBoxWrapper inputBuffer,
            BinaryValuesByte bvOut,
            Optional<Histogram> histogram,
            Optional<ObjectMask> mask)
            throws OperationFailedException {

        if (mask.isPresent()) {
            throw new OperationFailedException("A mask is not supported for this operation");
        }

        BinaryVoxelBox<ByteBuffer> thresholded =
                VoxelBoxThresholder.thresholdForLevel(
                        inputBuffer, minIntensity, bvOut, mask, false);

        Binary binaryPlugin = new Binary();
        binaryPlugin.setup("fill", null);
        binaryPlugin.setNPasses(1);

        for (int z = 0; z < thresholded.extent().getZ(); z++) {
            ImageProcessor ip =
                    IJWrap.imageProcessor(new VoxelBoxWrapper(thresholded.getVoxelBox()), z);
            binaryPlugin.run(ip);
        }

        return thresholded;
    }
}
