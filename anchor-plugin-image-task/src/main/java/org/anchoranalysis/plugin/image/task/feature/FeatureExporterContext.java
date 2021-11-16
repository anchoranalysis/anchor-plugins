package org.anchoranalysis.plugin.image.task.feature;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.feature.io.csv.FeatureCSVMetadata;
import org.anchoranalysis.feature.io.csv.FeatureCSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

@Value
@AllArgsConstructor
public class FeatureExporterContext {

    /** Context for reading from or writing outputs to the file-system. */
    private final InputOutputContext context;

    /**
     * When true, columns containing all {@link Double#NaN} values are removed before outputting.
     */
    private final boolean removeNaNColumns;

    /**
     * When true {@code double} values are printed to be as short as possible without losing
     * precision.
     */
    private final boolean visuallyShortenDecimals;
    
    /** If false, an image is reported as errored, if any exception is thrown during calculation.  If true, then a value of {@link Double#NaN} is returned, and a message is written to the error-log. */
    private final boolean suppressErrors;

    /** Creates a {@link FeatureCSVWriter} for the non-aggregated results. */
    public Optional<FeatureCSVWriter> csvWriter(FeatureCSVMetadata metadata)
            throws OutputWriteFailedException {
        return FeatureCSVWriter.create(metadata, context.getOutputter(), visuallyShortenDecimals);
    }
}
