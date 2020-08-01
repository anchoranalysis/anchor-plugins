package org.anchoranalysis.plugin.image.task.feature;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @author Owen Feehan
 *
 * @param <S> row-source that is duplicated for each new thread (to prevent any concurrency issues)
 */
@AllArgsConstructor
public class InputProcessContext<S> {

    private static final String OUTPUT_THUMBNAILS = "thumbnails";
    
    ExportFeatureResultsAdder adder;
    
    @Getter S rowSource;
    
    @Getter FeatureNameList featureNames;
    
    @Getter Optional<String> groupGeneratorName;
    
    @Getter BoundIOContext context;
    
    public Path getModelDirectory() {
        return context.getModelDirectory();
    }
    public Logger getLogger() {
        return context.getLogger();
    }
    public void addResultsFor(StringLabelsForCsvRow labels, ResultsVectorWithThumbnail results) {
        adder.addResultsFor(labels, results);
    }
    
    public boolean isThumbnails() {
        return context.getOutputManager().isOutputAllowed(OUTPUT_THUMBNAILS);
    }
}
