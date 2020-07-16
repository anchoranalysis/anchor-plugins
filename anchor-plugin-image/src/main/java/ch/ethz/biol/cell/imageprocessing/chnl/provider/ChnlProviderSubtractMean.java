/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderSubtractMean extends ChnlProviderOneMask {

    // START BEAN PROPERTIES
    @BeanField private boolean subtractFromMaskOnly = true;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChnl(Channel chnl, Mask mask) throws CreateException {

        double mean = calculateMean(chnl, mask);

        int meanInt = (int) Math.round(mean);

        if (subtractFromMaskOnly) {
            subtractMeanMask(chnl, mask, meanInt);
        } else {
            subtractMeanAll(chnl, meanInt);
        }

        return chnl;
    }

    private double calculateMean(Channel chnl, Mask mask) {

        VoxelBox<ByteBuffer> vbMask = mask.getChannel().getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbIntensity = chnl.getVoxelBox().asByte();

        Extent e = vbMask.extent();

        BinaryValuesByte bvb = mask.getBinaryValues().createByte();

        double sum = 0.0;
        double cnt = 0;

        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bbMask = vbMask.getPixelsForPlane(z).buffer();
            ByteBuffer bbIntensity = vbIntensity.getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    if (bbMask.get(offset) == bvb.getOnByte()) {
                        int intens = ByteConverter.unsignedByteToInt(bbIntensity.get(offset));
                        sum += intens;
                        cnt++;
                    }

                    offset++;
                }
            }
        }

        if (cnt == 0) {
            return 0;
        }

        return sum / cnt;
    }

    private void subtractMeanMask(Channel chnl, Mask mask, int mean) {

        VoxelBox<ByteBuffer> vbMask = mask.getChannel().getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbIntensity = chnl.getVoxelBox().asByte();

        Extent e = vbMask.extent();

        BinaryValuesByte bvb = mask.getBinaryValues().createByte();

        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bbMask = vbMask.getPixelsForPlane(z).buffer();
            ByteBuffer bbIntensity = vbIntensity.getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    if (bbMask.get(offset) == bvb.getOnByte()) {
                        int intens = ByteConverter.unsignedByteToInt(bbIntensity.get(offset));
                        int intensSub = (intens - mean);

                        if (intensSub < 0) {
                            intensSub = 0;
                        }

                        bbIntensity.put(offset, (byte) intensSub);
                    }

                    offset++;
                }
            }
        }
    }

    private void subtractMeanAll(Channel chnl, int mean) {

        VoxelBox<ByteBuffer> vbIntensity = chnl.getVoxelBox().asByte();

        Extent e = vbIntensity.extent();

        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bbIntensity = vbIntensity.getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    int intens = ByteConverter.unsignedByteToInt(bbIntensity.get(offset));
                    int intensSub = (intens - mean);

                    if (intensSub < 0) {
                        intensSub = 0;
                    }

                    bbIntensity.put(offset, (byte) intensSub);

                    offset++;
                }
            }
        }
    }

    public boolean isSubtractFromMaskOnly() {
        return subtractFromMaskOnly;
    }

    public void setSubtractFromMaskOnly(boolean subtractFromMaskOnly) {
        this.subtractFromMaskOnly = subtractFromMaskOnly;
    }
}
