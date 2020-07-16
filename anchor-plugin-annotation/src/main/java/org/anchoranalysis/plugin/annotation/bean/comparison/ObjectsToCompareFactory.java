/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.IAddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ObjectsToCompareFactory {

    public static Optional<ObjectsToCompare> create(
            AnnotationComparisonInput<ProvidesStackInput> input,
            IAddAnnotation<?> addAnnotation,
            ImageDimensions dimensions,
            BoundIOContext context)
            throws JobExecutionException {

        // Both objects need to be found
        return OptionalUtilities.mapBoth(
                createObjects(true, "leftObj", addAnnotation, input, dimensions, context),
                createObjects(false, "rightObj", addAnnotation, input, dimensions, context),
                ObjectsToCompare::new);
    }

    private static Optional<ObjectCollection> createObjects(
            boolean left,
            String objName,
            IAddAnnotation<?> addAnnotation,
            AnnotationComparisonInput<ProvidesStackInput> input,
            ImageDimensions dimensions,
            BoundIOContext context)
            throws JobExecutionException {
        Findable<ObjectCollection> findable =
                createFindable(left, input, dimensions, context.isDebugEnabled());
        return foundOrLogAddUnnannotated(findable, objName, addAnnotation, context.getLogger());
    }

    private static Optional<ObjectCollection> foundOrLogAddUnnannotated(
            Findable<ObjectCollection> objects,
            String objName,
            IAddAnnotation<?> addAnnotation,
            Logger logger) {
        Optional<ObjectCollection> found = objects.getFoundOrLog(objName, logger);
        if (!found.isPresent()) {
            addAnnotation.addUnannotatedImage();
        }
        return found;
    }

    private static Findable<ObjectCollection> createFindable(
            boolean left,
            AnnotationComparisonInput<ProvidesStackInput> input,
            ImageDimensions dimensions,
            boolean debugMode)
            throws JobExecutionException {
        try {
            return input.getComparerMultiplex(left)
                    .createObjects(input.pathForBindingRequired(), dimensions, debugMode);
        } catch (CreateException | AnchorIOException e) {
            throw new JobExecutionException(e);
        }
    }
}
