/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.object.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Creates object masks from a {@link Cfg} where (a particular region of) each mark creates an
 * object.
 *
 * @author Owen Feehan
 */
public class FromCfg extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField private CfgProvider cfgProvider;

    @BeanField private int regionID = 0;

    @BeanField private RegionMap regionMap = RegionMapSingleton.instance();

    @BeanField private ImageDimProvider dim = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        Cfg cfg = cfgProvider.create();

        ImageDimensions dims = dim.create();

        return cfg.calcMask(
                        dims,
                        regionMap.membershipWithFlagsForIndex(regionID),
                        BinaryValuesByte.getDefault())
                .withoutProperties();
    }

    public CfgProvider getCfgProvider() {
        return cfgProvider;
    }

    public void setCfgProvider(CfgProvider cfgProvider) {
        this.cfgProvider = cfgProvider;
    }

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }

    public ImageDimProvider getDim() {
        return dim;
    }

    public void setDim(ImageDimProvider dim) {
        this.dim = dim;
    }
}
