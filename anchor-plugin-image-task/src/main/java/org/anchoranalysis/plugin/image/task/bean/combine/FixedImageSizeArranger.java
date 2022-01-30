package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.bean.spatial.arrange.StackArranger;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.image.bean.spatial.arrange.tile.Tile;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.apache.commons.math3.util.Pair;

/**
 * Creates a {@link StackArranger} when the image-size <b>is not allowed vary</b>.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class FixedImageSizeArranger {

    /** If no scaling-factor is suggested, this determines the size of each image. */
    private final ScaleCalculator fixedSizeScaler;

    /** How to align each image in the corresponding table cell. */
    private final BoxAligner aligner;

    /**
     * Create a {@link StackArranger} when image-size is <b>not</b> allowed vary (from its initial
     * size).
     *
     * @param numberRows the number of rows in the montage.
     * @param suggestion any suggestion on the size of the combined-image.
     * @param imageSizes the sizes of all images (and their corresponding paths). Each element is
     *     altered during the call to this function.
     * @return a newly created {@link StackArranger}.
     * @throws OperationFailedException if an invalid {@code suggestedSize} is assigned.
     */
    public StackArranger create(
            int numberRows,
            Optional<ImageSizeSuggestion> suggestion,
            List<Pair<Path, Extent>> imageSizes)
            throws OperationFailedException {
        // Apply a scaling factor to the images, as we don't wish to use their original sizes
        scaleAllImages(imageSizes, suggestion);

        int numberColumns = calculateNumberColumns(imageSizes.size(), numberRows);

        // A strictly tabular form, where each image must fit inside its cell-size.
        Tile tile = new Tile();
        tile.setNumberColumns(numberColumns);
        tile.setNumberRows(numberRows);
        tile.setAligner(aligner);
        return tile;
    }

    /** Scale each element in {@code imageSizes}. */
    private void scaleAllImages(
            List<Pair<Path, Extent>> imageSizes, Optional<ImageSizeSuggestion> suggestion)
            throws OperationFailedException {
        Optional<ScaleFactor> scaleFactor =
                OptionalUtilities.map(suggestion, FixedImageSizeArranger::extractScaleFactor);

        for (int i = 0; i < imageSizes.size(); i++) {
            imageSizes.set(i, scalePair(imageSizes.get(i), scaleFactor));
        }
    }

    /** Scales the {@link Extent} in a pair. */
    private Pair<Path, Extent> scalePair(
            Pair<Path, Extent> pair, Optional<ScaleFactor> scaleFactor) {
        try {
            if (scaleFactor.isPresent()) {
                Extent second = pair.getSecond().scaleXYBy(scaleFactor.get(), true);
                return new Pair<>(pair.getFirst(), second);
            } else {
                Dimensions dimensions = new Dimensions(pair.getSecond());
                ScaleFactor factor =
                        fixedSizeScaler.calculate(Optional.of(dimensions), Optional.empty());
                Extent second = pair.getSecond().scaleXYBy(factor, true);
                return new Pair<>(pair.getFirst(), second);
            }
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    /**
     * Extracts a uniform {@link ScaleFactor} from {@code suggestion} throwing a {@link
     * OperationFailedException} if none is available.
     */
    private static ScaleFactor extractScaleFactor(ImageSizeSuggestion suggestion)
            throws OperationFailedException {
        Optional<ScaleFactor> factor = suggestion.uniformScaleFactor();
        return factor.orElseThrow(
                () ->
                        new OperationFailedException(
                                "Only a constant scaling-factor is supported for this operation. Scaling that resizes to a specific width or height is unsupported."));
    }

    /** Calculates the number of columns to use in the table. */
    private static int calculateNumberColumns(int totalNumberImages, int numberRows) {
        return (int) Math.ceil(((double) totalNumberImages) / numberRows);
    }
}
