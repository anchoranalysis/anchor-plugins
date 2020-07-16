/* (C)2020 */
package org.anchoranalysis.gui.annotation.bean.label;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.io.bean.color.RGBColorBean;

public class AnnotationLabel extends AnchorBean<AnnotationLabel> {

    // START BEAN PROPERTIES
    /** Label that uniquely identifies the ID (for machine purposes) */
    @BeanField @Getter @Setter private String uniqueLabel;

    /** Descriptive user-friendly label displayed via GUI */
    @BeanField @Getter @Setter private String userFriendlyLabel;

    /** An optional color associated with the label when displayed via GUI */
    @BeanField @OptionalBean @Getter @Setter private RGBColorBean color;

    /** Specifies a group for the label (similar labels that are displayed together) */
    @BeanField @AllowEmpty @Getter @Setter private String group;
    // END BEAN PROPERTIES
}
