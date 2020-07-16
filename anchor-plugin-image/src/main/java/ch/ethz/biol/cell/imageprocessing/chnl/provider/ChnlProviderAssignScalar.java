/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;

/** Assigns a scalar to the portion of the image covered by a mask */
public class ChnlProviderAssignScalar extends ChnlProviderOneMask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double value;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChnl(Channel chnl, Mask mask) {
        assignScalar(chnl, mask, (int) value);
        return chnl;
    }

    private void assignScalar(Channel chnlSrc, Mask mask, int value) {
        chnlSrc.getVoxelBox()
                .any()
                .setPixelsCheckMask(CreateFromEntireChnlFactory.createObject(mask), value);
    }
}
