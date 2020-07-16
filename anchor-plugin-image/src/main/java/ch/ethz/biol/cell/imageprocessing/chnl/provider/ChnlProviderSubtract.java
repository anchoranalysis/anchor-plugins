/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderSubtract extends ChnlProviderTwoVoxelMapping {

    @Override
    protected void processVoxelBox(
            VoxelBox<ByteBuffer> vbOut, VoxelBox<ByteBuffer> vbIn1, VoxelBox<ByteBuffer> vbIn2) {

        for (int z = 0; z < vbOut.extent().getZ(); z++) {

            ByteBuffer in1 = vbIn1.getPixelsForPlane(z).buffer();
            ByteBuffer in2 = vbIn2.getPixelsForPlane(z).buffer();
            ByteBuffer out = vbOut.getPixelsForPlane(z).buffer();

            while (in1.hasRemaining()) {

                byte b1 = in1.get();
                byte b2 = in2.get();

                int diff =
                        ByteConverter.unsignedByteToInt(b1) - ByteConverter.unsignedByteToInt(b2);
                out.put((byte) diff);
            }

            assert (!in2.hasRemaining());
            assert (!out.hasRemaining());
        }
    }
}
