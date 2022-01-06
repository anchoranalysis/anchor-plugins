package org.anchoranalysis.plugin.image.task.bean.slice.combine;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.ArrangeStackException;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.StackArrangement;
import org.anchoranalysis.image.bean.spatial.arrange.StackArranger;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.plugin.image.task.slice.MontageSharedState;
import org.anchoranalysis.spatial.box.BoundingBox;
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
     * @param paths the paths to include in the shared-state.
     * @param arranger how to arrange the images into a {@link StackArrangement}.
     * @param resizer how to resize an image.
     * @param deriveExtent derives the suggested size for an image from its {@link Path}, to be
     *     passed into {@code arranger}.
     * @return the newly created shared-state.
     * @throws ExperimentExecutionException if a stack arrangement cannot be successfully
     *     determined.
     */
    public static MontageSharedState create(
            Stream<Path> paths,
            StackArranger arranger,
            VoxelsResizer resizer,
            CheckedFunction<Path, Extent, ExperimentExecutionException> deriveExtent)
            throws ExperimentExecutionException {

        // The sizes for each image to place into the arrangement (e.g. table)
        List<Pair<Path, Extent>> sizes = deriveExtentForAllInputs(paths, deriveExtent);

        try {
            StackArrangement arrangement =
                    arranger.arrangeStacks(sizes.stream().map(Pair::getSecond).iterator());

            // Create the shared state with the bounding-box mapping and overall size for the
            // combined image.
            return new MontageSharedState(
                    mapFromArrangement(sizes, arrangement), arrangement.extent(), resizer);
        } catch (ArrangeStackException e) {
            throw new ExperimentExecutionException("Cannot determine a stack arrangement for", e);
        }
    }

    /** Map each path to the corresponding bounding-box from the arrangement. */
    private static Map<Path, BoundingBox> mapFromArrangement(
            List<Pair<Path, Extent>> sizes, StackArrangement arrangement) {
        Map<Path, BoundingBox> boxMapping = new HashMap<>();
        for (int i = 0; i < sizes.size(); i++) {
            boxMapping.put(sizes.get(i).getFirst(), arrangement.get(i));
        }
        return boxMapping;
    }

    /** Extract a {@link Extent} for each {@link Path} representing an input image. */
    private static List<Pair<Path, Extent>> deriveExtentForAllInputs(
            Stream<Path> paths,
            CheckedFunction<Path, Extent, ExperimentExecutionException> deriveExtent)
            throws ExperimentExecutionException {
        return FunctionalList.mapToList(
                paths,
                ExperimentExecutionException.class,
                path -> new Pair<>(path, deriveExtent.apply(path)));
    }
}
