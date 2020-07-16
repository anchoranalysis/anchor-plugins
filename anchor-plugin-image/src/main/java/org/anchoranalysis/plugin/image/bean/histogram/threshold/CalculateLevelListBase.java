/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;

@EqualsAndHashCode(callSuper = false)
public abstract class CalculateLevelListBase extends CalculateLevel {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<CalculateLevel> list = new ArrayList<>();
    // END BEAN PROPERTIES
}
