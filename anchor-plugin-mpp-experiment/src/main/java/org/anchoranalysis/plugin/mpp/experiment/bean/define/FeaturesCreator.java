package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.image.feature.bean.object.single.CenterOfGravity;
import org.anchoranalysis.image.feature.bean.object.single.NumberVoxels;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Creates certain hard-coded features
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class FeaturesCreator {

    /**
     * Creates default features for describing the results of instance-segmentation
     * 
     * @return
     */
    public static NamedFeatureStore<FeatureInputSingleObject> defaultInstanceSegmentation() {
        NamedFeatureStore<FeatureInputSingleObject> store = new NamedFeatureStore<>();
        store.add("centerOfGravity.x", new CenterOfGravity(AxisType.X) );
        store.add("centerOfGravity.y", new CenterOfGravity(AxisType.Y) );
        store.add("centerOfGravity.z", new CenterOfGravity(AxisType.Z) );
        store.add("numberVoxels", new NumberVoxels() );
        return store;
    }
}
