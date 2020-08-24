package org.anchoranalysis.plugin.image.feature.bean.object.single;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernelParameters;
import lombok.Getter;
import lombok.Setter;

public abstract class OutlineKernelBase extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean outsideAtThreshold = false;

    @BeanField @Getter @Setter private boolean ignoreAtThreshold = false;

    @BeanField @Getter @Setter private boolean do3D = false;
    // END BEAN PROPERTIES
    
    protected OutlineKernelParameters createParameters() {
        return new OutlineKernelParameters(outsideAtThreshold, do3D, ignoreAtThreshold);
    }
}
