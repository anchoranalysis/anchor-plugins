package org.anchoranalysis.plugin.image.task.size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Shared-state that exposes a common-size to all images.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@Accessors(fluent = true)
public class CommonSize {

    /** The common-size of all images. */
    @Getter private Extent extent;
}
