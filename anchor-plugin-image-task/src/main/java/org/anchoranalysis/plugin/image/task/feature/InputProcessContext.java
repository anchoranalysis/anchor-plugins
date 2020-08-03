package org.anchoranalysis.plugin.image.task.feature;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * @author Owen Feehan
 * @param <S> row-source that is duplicated for each new thread (to prevent any concurrency issues)
 */
public class InputProcessContext<S> {
    
    private static final String OUTPUT_THUMBNAILS = "thumbnails";

    // START REQUIRED ARGUMENTS
    ExportFeatureResultsAdder adder;

    @Getter S rowSource;

    @Getter FeatureNameList featureNames;

    @Getter Optional<String> groupGeneratorName;

    @Getter BoundIOContext context;
    // END REQUIRED ARGUMENTS
    
    @Getter boolean thumbnailsEnabled;

    public InputProcessContext(ExportFeatureResultsAdder adder, S rowSource,
            FeatureNameList featureNames, Optional<String> groupGeneratorName,
            BoundIOContext context) {
        super();
        this.adder = adder;
        this.rowSource = rowSource;
        this.featureNames = featureNames;
        this.groupGeneratorName = groupGeneratorName;
        this.context = context;
        this.thumbnailsEnabled = areThumbnailsEnabled(context);
    }
    
    public Path getModelDirectory() {
        return context.getModelDirectory();
    }

    public Logger getLogger() {
        return context.getLogger();
    }

    public void addResultsFor(StringLabelsForCsvRow labels, ResultsVectorWithThumbnail results) {
        adder.addResultsFor(labels, results);
    }
    
    private boolean areThumbnailsEnabled(BoundIOContext context) {
        return context.getOutputManager().isOutputAllowed(OUTPUT_THUMBNAILS);
    }
}
