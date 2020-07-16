/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.outline.FindOutline;

/**
 * Always creates a new BinaryChnl for the result (no need to duplicate input)
 *
 * @author feehano
 */
public class BinaryChnlProviderOutline extends BinaryChnlProviderOne {

    // START
    @BeanField private boolean force2D = false;

    @BeanField private boolean erodeEdges = true;
    // END

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {
        boolean do3D = chnl.getDimensions().getZ() > 1 && !force2D;
        return FindOutline.outline(chnl, do3D, erodeEdges);
    }

    public boolean isForce2D() {
        return force2D;
    }

    public void setForce2D(boolean force2d) {
        force2D = force2d;
    }

    public boolean isErodeEdges() {
        return erodeEdges;
    }

    public void setErodeEdges(boolean erodeEdges) {
        this.erodeEdges = erodeEdges;
    }
}
