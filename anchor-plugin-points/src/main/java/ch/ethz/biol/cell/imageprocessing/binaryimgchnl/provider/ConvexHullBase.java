/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.outline.FindOutline;

public abstract class ConvexHullBase extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private boolean erodeEdges = false;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromChnl(Mask chnlIn) throws CreateException {
        return createFromChnl(chnlIn, FindOutline.outline(chnlIn, true, erodeEdges));
    }

    protected abstract Mask createFromChnl(Mask chnlIn, Mask outline) throws CreateException;

    public boolean isErodeEdges() {
        return erodeEdges;
    }

    public void setErodeEdges(boolean erodeEdges) {
        this.erodeEdges = erodeEdges;
    }
}
