package org.anchoranalysis.plugin.quick.bean.structure;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;

public class RootedExperiment extends AnchorBean<RootedExperiment> {

    // START BEAN PROPERTIES
    /**
     * A directory identifying the type of experiment.
     *
     * <p>This assumes that all such the outputs are all put in the same directory.
     */
    @BeanField @Getter @Setter private String experimentType;

    /** Root-name assuming multi-rooted structure. */
    @BeanField @Getter @Setter private String rootName;
    // END BEAN PROPERTIES
}
