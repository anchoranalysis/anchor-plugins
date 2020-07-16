/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.mask.MaskInverter;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;

public class BinaryChnlProviderInvert extends BinaryChnlProviderOne {

    // START BEAN FIELDS
    @BeanField @OptionalBean @Getter @Setter private BinaryChnlProvider mask;

    @BeanField @Getter @Setter private boolean forceChangeBytes = false;
    // END BEAN FIELDS

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {

        Optional<Mask> maskChnl = OptionalFactory.create(mask);

        if (maskChnl.isPresent()) {
            invertWithMask(chnl, maskChnl.get());
            return chnl;
        }

        if (forceChangeBytes) {
            MaskInverter.invertChnl(chnl);
        } else {
            return new Mask(chnl.getChannel(), chnl.getBinaryValues().createInverted());
        }
        return chnl;
    }

    private void invertWithMask(Mask chnl, Mask mask) {

        BinaryValuesByte bvb = chnl.getBinaryValues().createByte();
        final byte byteOn = bvb.getOnByte();
        final byte byteOff = bvb.getOffByte();

        IterateVoxels.callEachPoint(
                chnl.binaryVoxelBox().getVoxelBox(),
                mask,
                (Point3i point, ByteBuffer buffer, int offset) -> {
                    byte val = buffer.get(offset);

                    if (val == byteOn) {
                        buffer.put(offset, byteOff);
                    } else {
                        buffer.put(offset, byteOn);
                    }
                });
    }
}
