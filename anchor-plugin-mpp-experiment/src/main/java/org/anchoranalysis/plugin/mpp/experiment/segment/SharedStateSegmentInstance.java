package org.anchoranalysis.plugin.mpp.experiment.segment;

import java.io.IOException;
import java.util.Optional;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.io.csv.LabelHeaders;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;
import lombok.Getter;

/**
 * Shared-state for instance segmentation
 * 
 * @author Owen Feehan
 * @param <T> model-type in pool
 */
public class SharedStateSegmentInstance<T> {

    private final SharedStateExportFeatures<FeatureTableCalculator<FeatureInputSingleObject>> features;
    @Getter private final ConcurrentModelPool<T> modelPool;
    
    public SharedStateSegmentInstance( ConcurrentModelPool<T> modelPool, FeatureTableCalculator<FeatureInputSingleObject> featureTable, LabelHeaders identifierHeaders, BoundIOContext context ) throws CreateException {
        this.modelPool = modelPool;
        this.features = SharedStateExportFeatures.createForFeatures(featureTable, identifierHeaders, context);
    }

    public InputProcessContext<FeatureTableCalculator<FeatureInputSingleObject>> createInputProcessContext(
            Optional<String> groupName, BoundIOContext context) {
        return features.createInputProcessContext(groupName, context);
    }

    public void closeAnyOpenIO() throws IOException {
        features.closeAnyOpenIO();
    }
}
