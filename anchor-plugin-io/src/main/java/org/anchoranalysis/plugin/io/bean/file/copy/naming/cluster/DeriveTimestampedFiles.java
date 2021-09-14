package org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.math.statistics.MeanScale;
import org.anchoranalysis.math.statistics.VarianceCalculatorDouble;
import org.anchoranalysis.plugin.io.bean.file.pattern.TimestampPattern;

/**
 * Derives a list of {@link TimestampedFile} from a list of {@link FileWithDirectoryInput}.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class DeriveTimestampedFiles {

    // START REQUIRED ARGUMENTS
    private final List<TimestampPattern> dateTimePatterns;
    // END REQUIRED ARGUMENTS

    /**
     * The mean-and-scale calculated from all {@code inputs}. This is null until after {@link
     * #derive} has been called.
     */
    private MeanScale scaler;

    /**
     * Derives the timestamps for the inputs.
     *
     * @param inputs the inputs to derive from.
     * @param offset the offset to assume the time-stamp belongs in.
     * @return a newly created list of timestamped files.
     * @throws OperationFailedException
     */
    public List<TimestampedFile> derive(List<FileWithDirectoryInput> inputs, ZoneOffset offset)
            throws OperationFailedException {
        VarianceCalculatorDouble varianceCalculator = new VarianceCalculatorDouble();

        try {
            List<TimestampedFile> extracted =
                    FunctionalList.mapToList(
                            inputs,
                            CreateException.class,
                            input ->
                                    new TimestampedFile(
                                            input.getFile(),
                                            varianceCalculator,
                                            dateTimePatterns,
                                            offset));
            scaler = deriveScaler(varianceCalculator);
            extracted.forEach(attributes -> attributes.normalize(scaler));
            return extracted;
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * The mean-and-scale calculated from all {@code inputs}.
     *
     * @return the scaler.
     * @throws OperationFailedException if {@link #derive(List,ZoneOffset)} has not yet been called.
     */
    public MeanScale getScaler() throws OperationFailedException {
        return Optional.ofNullable(scaler)
                .orElseThrow(() -> new OperationFailedException("No scaler is yet available"));
    }

    /** Calculates the mean and standard-deviation from a {@link VarianceCalculatorDouble}. */
    private static MeanScale deriveScaler(VarianceCalculatorDouble varianceCalculator) {
        double stdDev = Math.sqrt(varianceCalculator.variance());
        return new MeanScale(varianceCalculator.mean(), stdDev);
    }
}
