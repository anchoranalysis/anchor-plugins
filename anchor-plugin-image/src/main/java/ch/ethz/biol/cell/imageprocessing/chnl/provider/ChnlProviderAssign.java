/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;

/**
 * Copies the pixels from chnlAssignFrom to chnl (possibly masking)
 *
 * <p>chnl is changed (mutable). chnlAssignFrom is unchanged (immutable)..
 *
 * @author Owen Feehan
 */
public class ChnlProviderAssign extends ChnlProviderOneMask {

    // START BEAN PROPERTIES
    @BeanField private ChnlProvider chnlAssignFrom;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChnl(Channel chnlSrc, Mask binaryImgChnl)
            throws CreateException {

        assign(
                chnlSrc,
                DimChecker.createSameSize(chnlAssignFrom, "chnlAssignFrom", chnlSrc),
                binaryImgChnl);

        return chnlSrc;
    }

    private void assign(Channel chnlSrc, Channel chnlAssignFrom, Mask mask) {

        ObjectMask object = CreateFromEntireChnlFactory.createObject(mask);
        BoundingBox bbox = new BoundingBox(chnlSrc.getDimensions().getExtent());

        chnlAssignFrom
                .getVoxelBox()
                .asByte()
                .copyPixelsToCheckMask(
                        bbox,
                        chnlSrc.getVoxelBox().asByte(),
                        bbox,
                        object.getVoxelBox(),
                        object.getBinaryValuesByte());
    }

    public ChnlProvider getChnlAssignFrom() {
        return chnlAssignFrom;
    }

    public void setChnlAssignFrom(ChnlProvider chnlAssignFrom) {
        this.chnlAssignFrom = chnlAssignFrom;
    }
}
