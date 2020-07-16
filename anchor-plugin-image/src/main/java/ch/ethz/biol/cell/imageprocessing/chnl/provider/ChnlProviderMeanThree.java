/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderThree;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class ChnlProviderMeanThree extends ChnlProviderThree {

    @Override
    protected Channel process(Channel chnl1, Channel chnl2, Channel chnl3) throws CreateException {

        checkDims(chnl1, chnl2, chnl3);

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(
                                chnl1.getDimensions(), VoxelDataTypeUnsignedByte.INSTANCE);

        processVoxelBox(
                chnlOut.getVoxelBox().asByte(),
                chnl1.getVoxelBox().asByte(),
                chnl2.getVoxelBox().asByte(),
                chnl3.getVoxelBox().asByte());

        return chnlOut;
    }

    private void processVoxelBox(
            VoxelBox<ByteBuffer> vbOut,
            VoxelBox<ByteBuffer> vbIn1,
            VoxelBox<ByteBuffer> vbIn2,
            VoxelBox<ByteBuffer> vbIn3) {

        for (int z = 0; z < vbOut.extent().getZ(); z++) {

            ByteBuffer in1 = vbIn1.getPixelsForPlane(z).buffer();
            ByteBuffer in2 = vbIn2.getPixelsForPlane(z).buffer();
            ByteBuffer in3 = vbIn3.getPixelsForPlane(z).buffer();
            ByteBuffer out = vbOut.getPixelsForPlane(z).buffer();

            while (in1.hasRemaining()) {

                byte b1 = in1.get();
                byte b2 = in2.get();
                byte b3 = in3.get();

                int i1 = ByteConverter.unsignedByteToInt(b1);
                int i2 = ByteConverter.unsignedByteToInt(b2);
                int i3 = ByteConverter.unsignedByteToInt(b3);

                int mean = (i1 + i2 + i3) / 3;

                out.put((byte) mean);
            }

            assert (!in2.hasRemaining());
            assert (!in3.hasRemaining());
            assert (!out.hasRemaining());
        }
    }

    private void checkDims(Channel chnl1, Channel chnl2, Channel chnl3) throws CreateException {

        if (!chnl1.getDimensions().equals(chnl2.getDimensions())) {
            throw new CreateException("Dimensions of channels do not match");
        }

        if (!chnl2.getDimensions().equals(chnl3.getDimensions())) {
            throw new CreateException("Dimensions of channels do not match");
        }
    }
}
