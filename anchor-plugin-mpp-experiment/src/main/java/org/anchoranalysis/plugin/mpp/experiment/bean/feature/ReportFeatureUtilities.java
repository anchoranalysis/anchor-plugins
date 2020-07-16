/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.bean.report.feature.ReportFeature;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ReportFeatureUtilities {

    public static <T> List<String> genHeaderNames(
            List<? extends ReportFeature<T>> list, Logger logger) {

        // Create a list of headers
        List<String> headerNames = new ArrayList<>();
        for (ReportFeature<T> feat : list) {
            String name;
            try {
                name = feat.genTitleStr();
            } catch (OperationFailedException e) {
                name = "error";
                logger.errorReporter().recordError(ReportFeatureUtilities.class, e);
            }
            headerNames.add(name);
        }
        return headerNames;
    }

    public static <T> List<TypedValue> genElementList(
            List<? extends ReportFeature<T>> list, T obj, Logger logger) {

        List<TypedValue> rowElements = new ArrayList<>();

        for (ReportFeature<T> feat : list) {
            String value;
            try {
                value = feat.genFeatureStringFor(obj, logger);
            } catch (OperationFailedException e) {
                value = "error";
                logger.errorReporter().recordError(ReportFeatureUtilities.class, e);
            }

            rowElements.add(new TypedValue(value, feat.isNumeric()));
        }

        return rowElements;
    }
}
