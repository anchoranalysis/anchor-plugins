/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.exception.BeanDuplicateException;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.initializable.CheckMisconfigured;
import org.anchoranalysis.bean.permute.ApplyPermutations;
import org.anchoranalysis.bean.permute.property.PermuteProperty;
import org.anchoranalysis.bean.permute.setter.PermutationSetter;
import org.anchoranalysis.bean.permute.setter.PermutationSetterException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.shared.FeaturesInitialization;

/**
 * Permutes one or more properties of a Feature, so as to create a set of Features
 *
 * <p>As a convenience, a single permutation can be defined using the {@code permutation} property,
 * whereas multiple permutations can be defined using {@code permutations} list of properties.
 *
 * @author Owen Feehan
 * @param <S> permutation type
 * @param <T> feature-input
 */
public class PermuteFeature<S, T extends FeatureInput> extends PermuteFeatureBase<T> {

    // START BEAN PROPERTIES
    /** Makes sure a particular feature list creator is evaluated */
    @BeanField @OptionalBean @Getter @Setter private StringSet referencesFeatureListCreator;

    /**
     * A permutation to apply. Either this must be present or {@code permutations} must be
     * non-empty, but not both.
     */
    @BeanField @OptionalBean @Getter @Setter private PermuteProperty<S> permutation;

    /**
     * A list of permutations to apply. Either this must be non-empty or {@code permutation} must be
     * present, but not both.
     */
    @BeanField @Getter @Setter private List<PermuteProperty<S>> permutations = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        CheckMisconfigured.oneOnly(
                "permutation", "permutations", permutation != null, !permutations.isEmpty());
    }

    @Override
    protected FeatureList<T> createPermutedFeaturesFor(Feature<T> feature) throws ProvisionFailedException {

        // Create many copies of 'item' with properties adjusted
        List<Feature<T>> list = createInitialList(feature).asList();
        for (PermuteProperty<S> pp : permutationsAsList()) {

            try {
                PermutationSetter permutationSetter = pp.createSetter(feature);

                list =
                        new ApplyPermutations<Feature<T>>(
                                        Feature::getCustomName,
                                        (feat, name) -> feat.setCustomName(name))
                                .applyPermutationsToCreateDuplicates(list, pp, permutationSetter);
            } catch (PermutationSetterException e) {
                throw new ProvisionFailedException(
                        String.format("Cannot create a permutation-setter for %s", pp), e);
            } catch (CreateException e) {
                throw new ProvisionFailedException(e);
            }
        }

        return FeatureListFactory.wrapReuse(list);
    }

    @Override
    public void onInit(FeaturesInitialization so) throws InitException {
        super.onInit(so);
        if (referencesFeatureListCreator != null && so != null) {
            for (String s : referencesFeatureListCreator.set()) {

                try {
                    getInitialization().getFeatureListSet().getException(s);
                } catch (NamedProviderGetException e) {
                    throw new InitException(e.summarize());
                }
            }
        }
    }

    /** Multiplexes between the single-permutation property or the permutation-list property */
    private List<PermuteProperty<S>> permutationsAsList() {
        if (!permutations.isEmpty()) {
            return permutations;
        } else {
            return Arrays.asList(permutation);
        }
    }

    private FeatureList<T> createInitialList(Feature<T> feature) throws ProvisionFailedException {
        try {
            return FeatureListFactory.from(duplicateAndRemoveName(feature));
        } catch (BeanDuplicateException e) {
            throw new ProvisionFailedException(e);
        }
    }

    private Feature<T> duplicateAndRemoveName(Feature<T> feature) {
        // We add our item to fl as the 'input' item, knowing there's at least one permutation
        // The named doesn't matter, as will be replaced by next permutation
        return feature.duplicateChangeName("");
    }
}
