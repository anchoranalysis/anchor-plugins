/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.task.feature.fixture;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputResults;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.test.TestLoader;

/**
 * Loads features-lists of different types from particular XML files.
 *
 * @author Owen Feehan
 */
public class FeaturesLoader {

    /** The "single" features in use. */
    private LoadFeatures<FeatureInputSingleObject> single;

    /** The "pair" features in use. */
    private LoadFeatures<FeatureInputPairObjects> pair;

    /** Features on the entire image (the entire stack). */
    private LoadFeatures<FeatureInputStack> image;

    /** An expanded set of image features compared to {@code image}. */
    private LoadFeatures<FeatureInputStack> imageExpanded;

    /** The features used for the shared-feature set. */
    private LoadFeatures<FeatureInput> shared;

    /** The features used for aggregating results. */
    private LoadFeatures<FeatureInputResults> aggregated;

    /**
     * Creates a new {@link FeaturesLoader}.
     *
     * @param loader the {@link TestLoader} to use for loading XML files
     */
    public FeaturesLoader(TestLoader loader) {
        this.single = new LoadFeatures<>(loader, "single");
        this.pair = new LoadFeatures<>(loader, "pair");
        this.image = new LoadFeatures<>(loader, "image");
        this.imageExpanded = new LoadFeatures<>(loader, "imageExpanded");
        this.shared = new LoadFeatures<>(loader, "shared");
        this.aggregated = new LoadFeatures<>(loader, "aggregated");
    }

    /**
     * Gets the list of single-object features.
     *
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s for {@link
     *     FeatureInputSingleObject}
     */
    public List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> single() {
        return single.asNamedBean();
    }

    /**
     * Gets the list of pair-object features.
     *
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s for {@link
     *     FeatureInputPairObjects}
     */
    public List<NamedBean<FeatureListProvider<FeatureInputPairObjects>>> pair() {
        return pair.asNamedBean();
    }

    /**
     * Gets the list of image features.
     *
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s for {@link
     *     FeatureInputStack}
     */
    public List<NamedBean<FeatureListProvider<FeatureInputStack>>> image() {
        return image.asNamedBean();
    }

    /**
     * Gets the expanded list of image features.
     *
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s for {@link
     *     FeatureInputStack}
     */
    public List<NamedBean<FeatureListProvider<FeatureInputStack>>> imageExpanded() {
        return imageExpanded.asNamedBean();
    }

    /**
     * Gets the list of shared features.
     *
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s for {@link
     *     FeatureInput}
     */
    public List<NamedBean<FeatureListProvider<FeatureInput>>> shared() {
        return shared.asNamedBean();
    }

    /**
     * Gets the list of aggregated features.
     *
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s for {@link
     *     FeatureInputResults}
     */
    public List<NamedBean<FeatureListProvider<FeatureInputResults>>> aggregated() {
        return aggregated.asNamedBean();
    }

    /** Changes the single-object features to reference shared features. */
    public void changeSingleToReferenceShared() {
        changeSingleTo("referenceWithShared");
    }

    /** Changes the single-object features to reference with include. */
    public void changeSingleToReferenceWithInclude() {
        changeSingleTo("referenceWithInclude");
    }

    /** Changes the single-object features to shell features. */
    public void changeSingleToShellFeatures() {
        changeSingleTo("singleWithShell");
    }

    /**
     * Uses a specific feature instead of the loaded list for single-object features.
     *
     * <p>It does not initialize the feature.
     *
     * @param feature the {@link Feature} to use for {@link FeatureInputSingleObject}
     */
    public void changeSingleTo(Feature<FeatureInputSingleObject> feature) {
        single.useSingleFeature(feature);
    }

    /**
     * Uses a specific feature instead of the loaded list for pair-object features.
     *
     * <p>It does not initialize the feature.
     *
     * @param feature the {@link Feature} to use for {@link FeatureInputPairObjects}
     */
    public void changePairTo(Feature<FeatureInputPairObjects> feature) {
        pair.useSingleFeature(feature);
    }

    /**
     * Uses a specific feature instead of the loaded list for image features.
     *
     * <p>It does not initialize the feature.
     *
     * @param feature the {@link Feature} to use for {@link FeatureInputStack}
     */
    public void changeImageTo(Feature<FeatureInputStack> feature) {
        image.useSingleFeature(feature);
    }

    /**
     * Changes the single-object features to an alternative XML list.
     *
     * @param alternativeFileName the name of the alternative XML file to use
     */
    private void changeSingleTo(String alternativeFileName) {
        single.useAlternativeXMLList(alternativeFileName);
    }
}
