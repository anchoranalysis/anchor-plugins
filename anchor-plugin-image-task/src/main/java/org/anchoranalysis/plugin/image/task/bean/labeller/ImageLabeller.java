/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.Set;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * @author Owen Feehan
 * @param <T> shared-state
 */
public abstract class ImageLabeller<T> extends AnchorBean<ImageLabeller<T>> {

    /**
     * Should be called once before calling any other methods
     *
     * @param pathForBinding a path that can be used by the labeller to make filePath decisions
     */
    public abstract T init(Path pathForBinding) throws InitException;

    /**
     * A set of identifiers for all groups that can be outputted by the labeller. Should be callable
     * always
     *
     * @param params TODO
     */
    public abstract Set<String> allLabels(T params);

    /**
     * Determines a particular group-identifier for an input
     *
     * @param sharedState TODO
     * @param modelDir TODO
     */
    public abstract String labelFor(T sharedState, ProvidesStackInput input, BoundIOContext context)
            throws OperationFailedException;
}
