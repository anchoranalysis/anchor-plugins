/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.plugin.image.object.merge.condition.BeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.condition.DistanceCondition;

/**
 * Expands {@link MergeBase} by optionally imposing a maximum-distance requirement between objects
 * that are possibly merged.
 *
 * @author Owen Feehan
 */
public abstract class MergeWithOptionalDistanceConstraint extends MergeBase {

    // START BEAN FIELDS
    @BeanField @Getter @Setter private boolean suppressZ = false;

    /** An optional maximum distance */
    @BeanField @OptionalBean @Getter @Setter private UnitValueDistance maxDistance;
    // END BEAN FIELDS

    protected BeforeCondition maybeDistanceCondition() {
        return new DistanceCondition(
                Optional.ofNullable(maxDistance), suppressZ, getLogger().messageLogger());
    }
}
