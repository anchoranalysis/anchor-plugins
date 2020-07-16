/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryChnlProviderNot extends BinaryChnlProviderReceive {

    // ASSUMES REGIONS ARE IDENTICAL
    @Override
    protected Mask createFromChnlReceive(Mask chnlCrnt, Mask chnlReceiver) throws CreateException {

        BinaryValuesByte bvbCrnt = chnlCrnt.getBinaryValues().createByte();
        BinaryValuesByte bvbReceiver = chnlReceiver.getBinaryValues().createByte();

        Extent e = chnlCrnt.getDimensions().getExtent();

        byte crntOn = bvbCrnt.getOnByte();
        byte crntOff = bvbCrnt.getOffByte();
        byte receiveOff = bvbReceiver.getOffByte();

        // All the on voxels in the receive, are put onto crnt
        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bufSrc = chnlCrnt.getVoxelBox().getPixelsForPlane(z).buffer();
            ByteBuffer bufReceive = chnlReceiver.getVoxelBox().getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    byte byteSrc = bufSrc.get(offset);
                    if (byteSrc == crntOn) {

                        byte byteRec = bufReceive.get(offset);
                        if (byteRec == receiveOff) {
                            bufSrc.put(offset, crntOn);
                        } else {
                            bufSrc.put(offset, crntOff);
                        }
                    }

                    offset++;
                }
            }
        }

        return chnlCrnt;
    }
}
