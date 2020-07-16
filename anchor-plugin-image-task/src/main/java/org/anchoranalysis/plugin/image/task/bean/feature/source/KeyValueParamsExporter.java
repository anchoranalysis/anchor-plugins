/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.generator.serialized.KeyValueParamsGenerator;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/** Exports a ResultVector as a KeyValueParams */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class KeyValueParamsExporter {

    public static void export(
            FeatureNameList featureNames, ResultsVector rv, BoundIOContext context) {
        KeyValueParams kvp = convert(featureNames, rv, context.getLogger());
        writeKeyValueParams(kvp, context.getOutputManager());
    }

    private static void writeKeyValueParams(
            KeyValueParams kvp, BoundOutputManagerRouteErrors outputManager) {
        outputManager
                .getWriterCheckIfAllowed()
                .write("keyValueParams", () -> new KeyValueParamsGenerator(kvp, "keyValueParams"));
    }

    private static KeyValueParams convert(
            FeatureNameList featureNames, ResultsVector rv, Logger logger) {
        assert (featureNames.size() == rv.length());

        KeyValueParams kv = new KeyValueParams();
        for (int i = 0; i < featureNames.size(); i++) {

            String key = featureNames.get(i);
            Optional<Double> val = rv.getDoubleOrNull(i);

            if (val.isPresent()) {
                kv.put(key, val.get());
            } else {
                // Then an error happened and we report it
                logger.errorReporter().recordError(FromHistogram.class, rv.getException(i));
                kv.put(key, Double.NaN);
            }
        }
        return kv;
    }
}
