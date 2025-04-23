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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.DefineSingle;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXMLFixture;

/**
 * Loads a feature-list to be used in tests.
 *
 * <p>By default, this comes from a particular XML file in the test-resources, but methods allow
 * other possibilities
 *
 * @param <T> feature-input-type returned by the list
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class LoadFeatures<T extends FeatureInput> {

    /** The name used for a single feature when {@link #useSingleFeature(Feature)} is called. */
    private static final String SINGLE_FEATURE_NAME = "someFeature";

    /** A loader for finding XML files. */
    private final TestLoader loader;

    /** The filename of a features XML file that is used if no other option is set. */
    private final String filenameWithoutExtension;

    /** The features to use, if set. If empty, the default is used instead. */
    private Optional<List<NamedBean<FeatureListProvider<T>>>> features = Optional.empty();

    /**
     * Uses an alternative XML file to load the feature list.
     *
     * @param alternativeFilenameWithoutExtension the filename (without extension) of the
     *     alternative XML file
     */
    public void useAlternativeXMLList(String alternativeFilenameWithoutExtension) {
        features = Optional.of(loadFeatures(loader, alternativeFilenameWithoutExtension));
    }

    /**
     * Uses this feature as the only item in the list.
     *
     * <p>Any XML-based features are ignored.
     *
     * <p>It does not initialize the feature.
     *
     * @param feature the single feature to use
     */
    public void useSingleFeature(Feature<T> feature) {
        NamedBean<FeatureListProvider<T>> bean =
                new NamedBean<>(SINGLE_FEATURE_NAME, new DefineSingle<>(feature));
        this.features = Optional.of(Arrays.asList(bean));
    }

    /**
     * Creates the features as a list of providers in named-beans.
     *
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s
     */
    public List<NamedBean<FeatureListProvider<T>>> asNamedBean() {
        if (features.isPresent()) {
            return features.get();
        } else {
            return loadFeatures(loader, filenameWithoutExtension);
        }
    }

    /**
     * Creates a feature-list associated with object-mask.
     *
     * @param <S> the type of feature input
     * @param loader the test loader to use
     * @param filenameWithoutExtension the filename (without extension) of the XML file containing
     *     the features
     * @return a list of {@link NamedBean}s containing {@link FeatureListProvider}s
     */
    private static <S extends FeatureInput> List<NamedBean<FeatureListProvider<S>>> loadFeatures(
            TestLoader loader, String filenameWithoutExtension) {
        String path = String.format("features/%s.xml", filenameWithoutExtension);
        try {
            return FeaturesFromXMLFixture.createNamedFeatureProviders(path, loader);
        } catch (CreateException e) {
            // Prefer run-time exceptions to avoid chaining in the tests
            throw new AnchorFriendlyRuntimeException(e);
        }
    }
}
