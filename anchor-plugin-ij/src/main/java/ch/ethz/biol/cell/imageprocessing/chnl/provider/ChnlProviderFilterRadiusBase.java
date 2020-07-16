/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.extent.ImageDimensions;

/** Groups some similar filters that use a 'radius' parameter */
public abstract class ChnlProviderFilterRadiusBase extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Positive private double radius = 2;

    @BeanField private boolean radiusInMeters = false; // Treats radius if it's microns
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        return createFromChnl(chnl, radiusInVoxels(chnl.getDimensions()));
    }

    protected abstract Channel createFromChnl(Channel chnl, int radius) throws CreateException;

    private int radiusInVoxels(ImageDimensions dim) {
        if (radiusInMeters) {
            // Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care of
            // later)
            return (int) Math.round(ImageUnitConverter.convertFromMeters(radius, dim.getRes()));
        } else {
            return (int) radius;
        }
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isRadiusInMeters() {
        return radiusInMeters;
    }

    public void setRadiusInMeters(boolean radiusInMeters) {
        this.radiusInMeters = radiusInMeters;
    }
}
