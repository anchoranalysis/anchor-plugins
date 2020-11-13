package org.anchoranalysis.plugin.image.task.bean.scale;

import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Named-collection of stacks, remembering either or both of a stack and it's maximum-intensity projection.
 * 
 * @author Owen Feehan
 */
@Value @Accessors(fluent=true) class DualNamedStacks {
    /** Named-collection of non-flattened stacks. */
    private NamedStacks nonFlattened = new NamedStacks();
    
    /** Named-collection of flattened stacks. */
    private NamedStacks flattened = new NamedStacks();
    
    /**
     * Adds a stack and/or it's maximum-intensity projection to the collection based on {@code dualEnabled}.
     * 
     * @param name name of the stack
     * @param stack the stack to add
     * @param dualEnabled whether non-flattend and flattened outputs are enabled.
     */
    public void addStack(String name, Stack stack, DualEnabled dualEnabled) {
        if (dualEnabled.isNonFlattened()) {
            nonFlattened.add(name, stack);
        }

        if (dualEnabled.isFlattened()) {
            flattened.add(name, stack.projectMax());
        }
    }
}