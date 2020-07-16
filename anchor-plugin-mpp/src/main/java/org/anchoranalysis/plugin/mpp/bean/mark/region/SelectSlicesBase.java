/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;

@EqualsAndHashCode(callSuper = false)
public abstract class SelectSlicesBase extends IndexedRegionBase {

    // START BEAN PROPERTIES
    /** Index of slice to use, or -1 to use all slices */
    @BeanField @Getter @Setter private int sliceID = -1;
    // END BEAN PROPERTIES

    @Override
    public String uniqueName() {
        return String.format("%s_%d", super.uniqueName(), sliceID);
    }
}
