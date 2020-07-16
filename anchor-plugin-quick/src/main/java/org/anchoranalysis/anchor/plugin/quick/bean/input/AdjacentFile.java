/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;

public class AdjacentFile extends AnchorBean<AdjacentFile> {

    // START BEAN PROPERTIES
    /** A name to describe the adjacent file */
    @BeanField private String name;

    /** The replacement string used to determine the file */
    @BeanField private String replacement;
    // END BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
}
