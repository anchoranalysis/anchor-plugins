/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolume;

public class VolumeMin extends CheckMark {

    // START BEAN PROPERTIES
    @BeanField private UnitValueVolume minVolume;

    @BeanField private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
    // END BEAN PROPERTIES

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {

        double vol = mark.volume(regionID);

        double volMin;
        try {
            volMin = minVolume.resolveToVoxels(Optional.of(nrgStack.getDimensions().getRes()));
        } catch (UnitValueException e) {
            throw new CheckException(
                    "The volume-min check, had a unit Value Exception: " + e.toString());
        }

        return vol > volMin;
    }

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }

    public UnitValueVolume getMinVolume() {
        return minVolume;
    }

    public void setMinVolume(UnitValueVolume minVolume) {
        this.minVolume = minVolume;
    }
}
