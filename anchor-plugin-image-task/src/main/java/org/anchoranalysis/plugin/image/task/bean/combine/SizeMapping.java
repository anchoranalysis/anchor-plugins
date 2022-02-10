package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Maps a {@link Path} to a particular image-size and orientation-change.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class SizeMapping {

    /** The path. */
    @Getter private Path path;

    /** The size. */
    @Getter private Extent extent;

    /**
     * Replace the existing {@link Extent} with a new value.
     *
     * @param extent the extent to assign.
     */
    public void assignExtent(Extent extent) {
        this.extent = extent;
    }
}
