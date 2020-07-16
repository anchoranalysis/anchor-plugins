/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.bean.permute.ApplyPermutations;
import org.anchoranalysis.bean.permute.property.PermuteProperty;
import org.anchoranalysis.bean.permute.setter.PermutationSetter;
import org.anchoranalysis.bean.permute.setter.PermutationSetterException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;

/**
 * Permutes one or more properties of a Feature, so as to create a set of Features
 *
 * @author Owen Feehan
 * @param S permutation type
 * @param T feature-input
 */
public class PermuteFeature<S, T extends FeatureInput> extends PermuteFeatureBase<T> {

    /** */

    // START BEAN PROPERTIES
    @BeanField @OptionalBean
    private StringSet
            referencesFeatureListCreator; // Makes sure a particular feature list creator is
    // evaluated

    @BeanField @NonEmpty private List<PermuteProperty<S>> listPermuteProperty = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    protected FeatureList<T> createPermutedFeaturesFor(Feature<T> feature) throws CreateException {

        // Create many copies of 'item' with properties adjusted
        List<Feature<T>> list = createInitialList(feature).asList();
        for (PermuteProperty<S> pp : listPermuteProperty) {

            try {
                PermutationSetter permutationSetter = pp.createSetter(feature);

                list =
                        new ApplyPermutations<Feature<T>>(
                                        Feature::getCustomName,
                                        (feat, name) -> feat.setCustomName(name))
                                .applyPermutationsToCreateDuplicates(list, pp, permutationSetter);
            } catch (PermutationSetterException e) {
                throw new CreateException(
                        String.format("Cannot create a permutation-setter for %s", pp), e);
            }
        }

        return FeatureListFactory.wrapReuse(list);
    }

    @Override
    public void onInit(SharedFeaturesInitParams so) throws InitException {
        super.onInit(so);
        if (referencesFeatureListCreator != null && so != null) {
            for (String s : referencesFeatureListCreator.set()) {

                try {
                    getInitializationParameters().getFeatureListSet().getException(s);
                } catch (NamedProviderGetException e) {
                    throw new InitException(e.summarize());
                }
            }
        }
    }

    private FeatureList<T> createInitialList(Feature<T> feature) throws CreateException {
        try {
            return FeatureListFactory.from(duplicateAndRemoveName(feature));
        } catch (BeanDuplicateException e) {
            throw new CreateException(e);
        }
    }

    private Feature<T> duplicateAndRemoveName(Feature<T> feature) {
        // We add our item to fl as the 'input' item, knowing there's at least one permutation
        // The named doesn't matter, as will be replaced by next permutation
        return feature.duplicateChangeName("");
    }

    public StringSet getReferencesFeatureListCreator() {
        return referencesFeatureListCreator;
    }

    public void setReferencesFeatureListCreator(StringSet referencesFeatureListCreator) {
        this.referencesFeatureListCreator = referencesFeatureListCreator;
    }

    public List<PermuteProperty<S>> getListPermuteProperty() {
        return listPermuteProperty;
    }

    public void setListPermuteProperty(List<PermuteProperty<S>> listPermuteProperty) {
        this.listPermuteProperty = listPermuteProperty;
    }
}
