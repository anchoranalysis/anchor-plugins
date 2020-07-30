package org.anchoranalysis.plugin.image.task.feature;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class ExportFeatureFromInputContext {

    FeatureNameList featureNames;
    Optional<String> groupGeneratorName;
    boolean thumbnails;
    BoundIOContext context;
    
    public Path getModelDirectory() {
        return context.getModelDirectory();
    }
    public Logger getLogger() {
        return context.getLogger();
    }
}
