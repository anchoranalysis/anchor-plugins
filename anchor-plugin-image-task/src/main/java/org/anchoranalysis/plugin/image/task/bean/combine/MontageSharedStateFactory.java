package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.ArrangeStackException;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.BoundingBoxEnclosed;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.StackArrangement;
import org.anchoranalysis.image.bean.spatial.arrange.StackArranger;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.plugin.image.task.slice.MontageSharedState;
import org.anchoranalysis.spatial.box.Extent;
import org.apache.commons.math3.util.Pair;

/**
 * Creates a {@link MontageSharedState}.
 *
 * @author Owen Feehan
 */
class MontageSharedStateFactory {

    /**
     * Create the {@link MontageSharedState}.
     *
     * @param sizes the size of each image to place into the arrangement, together with the path it
     *     was derived from.
     * @param arranger how to arrange the images into a {@link StackArrangement}.
     * @param resizer how to resize an image.
     * @return the newly created shared-state.
     * @throws ExperimentExecutionException if a stack arrangement cannot be successfully
     *     determined.
     */
    public static MontageSharedState create(
            List<Pair<Path, Extent>> sizes,
            StackArranger arranger,
            VoxelsResizer resizer,
            ExecutionTimeRecorder recorder)
            throws ExperimentExecutionException {

        try {
            StackArrangement arrangement =
                    recorder.recordExecutionTime(
                            "Arranging the stacks",
                            () ->
                                    arranger.arrangeStacks(
                                            sizes.stream().map(Pair::getSecond).iterator()));

            // Create the shared state with the bounding-box mapping and overall size for the
            // combined image.
            return new MontageSharedState(
                    mapFromArrangement(sizes, arrangement), arrangement.extent(), resizer);
        } catch (ArrangeStackException e) {
            throw new ExperimentExecutionException("Cannot determine a stack arrangement for", e);
        }
    }

    /** Map each path to the corresponding bounding-box from the arrangement. */
    private static Map<Path, BoundingBoxEnclosed> mapFromArrangement(
            List<Pair<Path, Extent>> sizes, StackArrangement arrangement) {
        Map<Path, BoundingBoxEnclosed> boxMapping = new HashMap<>();
        for (int i = 0; i < sizes.size(); i++) {
            boxMapping.put(sizes.get(i).getFirst(), arrangement.get(i));
        }
        return boxMapping;
    }
}
