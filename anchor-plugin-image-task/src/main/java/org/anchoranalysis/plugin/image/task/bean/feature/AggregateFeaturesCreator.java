package org.anchoranalysis.plugin.image.task.bean.feature;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.Define;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.bean.results.Count;
import org.anchoranalysis.feature.bean.results.FeatureResultsStatistic;
import org.anchoranalysis.feature.bean.results.Maximum;
import org.anchoranalysis.feature.bean.results.Mean;
import org.anchoranalysis.feature.bean.results.Minimum;
import org.anchoranalysis.feature.bean.results.StandardDeviation;
import org.anchoranalysis.feature.input.FeatureInputResults;
import org.anchoranalysis.feature.name.FeatureNameList;

/**
 * Creates <i>default</i> aggregate features to use, if no others are specified.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AggregateFeaturesCreator {

    /**
     * Creates a list of default aggregate-features, applied to a named list of features.
     *
     * <p>For each referenced features, the corresponding aggregations are created:
     *
     * <ul>
     *   <li>mean
     *   <li>standard-deviation
     *   <li>minimum
     *   <li>maximum
     * </ul>
     *
     * @param featuresToReference the names of the features to reference.
     * @return the newly created {@link List} of beans, to provide the aggregations above for each
     *     feature in {@code featuresToReference}.
     */
    public static List<NamedBean<FeatureListProvider<FeatureInputResults>>> createDefaultFeatures(
            FeatureNameList featuresToReference) {

        List<NamedBean<FeatureListProvider<FeatureInputResults>>> out = new ArrayList<>();

        addUnreferenced("count", new Count(), out);

        for (String featureName : featuresToReference) {
            addReferenced("mean", new Mean(), featureName, out);
            addReferenced("standardDeviation", new StandardDeviation(), featureName, out);
            addReferenced("min", new Minimum(), featureName, out);
            addReferenced("max", new Maximum(), featureName, out);
        }

        return out;
    }

    /**
     * Wraps a {@link FeatureResultsStatistic} into a named-bean, assigning a reference to another
     * feature.
     *
     * <p>The wrapped bean is added to {@code toAddTo}.
     *
     * @param name the name of the feature, to be assigned to the bean used to store it.
     * @param featureAggregate the feature that performs the aggregation.
     * @param referenedFeature the feature that is referenced by {@code featureAggregate}.
     * @param toAddTo the list to add the named-feature to.
     */
    private static void addReferenced(
            String name,
            FeatureResultsStatistic featureAggregate,
            String referenedFeature,
            List<NamedBean<FeatureListProvider<FeatureInputResults>>> toAddTo) {
        featureAggregate.setId(referenedFeature);
        addUnreferenced(name + "." + referenedFeature, featureAggregate, toAddTo);
    }

    /**
     * Wraps a {@code Feature<FeatureInputResults>} into a named-bean, without assigning any
     * reference.
     *
     * <p>The wrapped bean is added to {@code toAddTo}.
     *
     * @param name the name of the feature, to be assigned to the bean used to store it.
     * @param featureAggregate the feature that performs the aggregation.
     * @param toAddTo the list to add the named-feature to.
     */
    private static void addUnreferenced(
            String name,
            Feature<FeatureInputResults> featureAggregate,
            List<NamedBean<FeatureListProvider<FeatureInputResults>>> toAddTo) {
        NamedBean<FeatureListProvider<FeatureInputResults>> bean =
                new NamedBean<>(name, new Define<>(featureAggregate));
        toAddTo.add(bean);
    }
}
