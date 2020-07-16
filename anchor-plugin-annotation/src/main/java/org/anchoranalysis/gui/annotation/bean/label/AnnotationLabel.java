/* (C)2020 */
package org.anchoranalysis.gui.annotation.bean.label;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.io.bean.color.RGBColorBean;

public class AnnotationLabel extends AnchorBean<AnnotationLabel> {

    // START BEAN PROPERTIES
    /** Label that uniquely identifies the ID (for machine purposes) */
    @BeanField private String uniqueLabel;

    @BeanField
    /** Descriptive user-friendly label displayed via GUI */
    private String userFriendlyLabel;

    @BeanField @OptionalBean
    /** An optional color associated with the label when displayed via GUI */
    private RGBColorBean color;

    /** Specifies a group for the label (similar labels that are displayed together) */
    @BeanField @AllowEmpty private String group;
    // END BEAN PROPERTIES

    public String getUniqueLabel() {
        return uniqueLabel;
    }

    public void setUniqueLabel(String uniqueLabel) {
        this.uniqueLabel = uniqueLabel;
    }

    public String getUserFriendlyLabel() {
        return userFriendlyLabel;
    }

    public void setUserFriendlyLabel(String userFriendlyLabel) {
        this.userFriendlyLabel = userFriendlyLabel;
    }

    public RGBColorBean getColor() {
        return color;
    }

    public void setColor(RGBColorBean color) {
        this.color = color;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
