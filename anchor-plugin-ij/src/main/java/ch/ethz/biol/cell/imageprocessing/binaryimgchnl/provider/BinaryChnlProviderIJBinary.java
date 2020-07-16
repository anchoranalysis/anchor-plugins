/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import ij.Prefs;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;
import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderIJBinary extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField
    private String command = ""; // One of: open, close, fill, erode, dilate, skel, outline

    @BeanField @Positive private int iterations = 1; // iterations for erode, dilate, open, close
    // END BEAN PROPERTIES

    public static void fill(BinaryVoxelBox<ByteBuffer> bvb) throws OperationFailedException {
        doCommand(bvb, "fill", 1);
    }

    private static BinaryVoxelBox<ByteBuffer> doCommand(
            BinaryVoxelBox<ByteBuffer> bvb, String command, int iterations)
            throws OperationFailedException {

        if (bvb.getBinaryValues().getOnInt() != 255 || bvb.getBinaryValues().getOffInt() != 0) {
            throw new OperationFailedException("On byte must be 255, and off byte must be 0");
        }

        Prefs.blackBackground = true;

        // Fills Holes
        Binary binaryPlugin = new Binary();
        binaryPlugin.setup(command, null);
        binaryPlugin.setNPasses(bvb.extent().getZ());

        for (int i = 0; i < iterations; i++) {

            // Are we missing a Z slice?
            for (int z = 0; z < bvb.extent().getZ(); z++) {

                ImageProcessor ip =
                        IJWrap.imageProcessorByte(bvb.getVoxelBox().getPlaneAccess(), z);
                binaryPlugin.run(ip);
            }
        }

        return bvb;
    }

    @Override
    public Mask createFromChnl(Mask binaryChnl) throws CreateException {

        BinaryVoxelBox<ByteBuffer> bvb = binaryChnl.binaryVoxelBox();

        try {
            BinaryVoxelBox<ByteBuffer> bvbOut = doCommand(bvb, command, iterations);
            return new Mask(
                    bvbOut,
                    binaryChnl.getDimensions().getRes(),
                    ChannelFactory.instance().get(VoxelDataTypeUnsignedByte.INSTANCE));
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
