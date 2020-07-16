/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.labeller;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;

public class DescriptiveNameContainsImageLabeller extends BinaryOutcomeImageLabeller {

    // START BEAN PROPERTIES
    @BeanField private String contains;
    // END BEAN PROPERTIES

    @Override
    public String labelFor(
            NoSharedState sharedState, ProvidesStackInput input, BoundIOContext context)
            throws OperationFailedException {
        return classificationString(input.descriptiveName().contains(contains));
    }

    public String getContains() {
        return contains;
    }

    public void setContains(String contains) {
        this.contains = contains;
    }
}
