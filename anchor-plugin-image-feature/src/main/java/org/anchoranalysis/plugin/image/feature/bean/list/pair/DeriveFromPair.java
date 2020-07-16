/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.pair;

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.bean.list.FeatureListProviderPrependName;
import org.anchoranalysis.image.feature.bean.object.pair.FeatureDeriveFromPair;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.bean.object.pair.Merged;
import org.anchoranalysis.image.feature.bean.object.pair.Second;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Embeds each feature in a {@link FeatureDeriveFromPair} feature (<i>first</i>, <i>second</i> or
 * <i>merge</i>) and prepends with a string.
 *
 * @author Owen Feehan
 */
public class DeriveFromPair extends FeatureListProvider<FeatureInputPairObjects> {

    // START BEAN PROPERTIES
    @BeanField private FeatureListProvider<FeatureInputSingleObject> item;

    @BeanField @AllowEmpty private String prependString = "";

    /** Either "merged" or "first" or "second" indicating which feature to delegate from */
    @BeanField private String select;
    // END BEAN PROPERTIES

    @Override
    public FeatureList<FeatureInputPairObjects> create() throws CreateException {
        return item.create().map(this::pairFromSingle);
    }

    private FeatureDeriveFromPair pairFromSingle(Feature<FeatureInputSingleObject> featExst)
            throws CreateException {
        Feature<FeatureInputSingleObject> featExstDup = featExst.duplicateBean();

        FeatureDeriveFromPair featDelegate = createNewDelegateFeature();
        featDelegate.setItem(featExstDup);

        FeatureListProviderPrependName.setNewNameOnFeature(
                featDelegate, featExstDup.getFriendlyName(), prependString);
        return featDelegate;
    }

    private FeatureDeriveFromPair createNewDelegateFeature() throws CreateException {
        if (select.equalsIgnoreCase("first")) {
            return new First();
        } else if (select.equalsIgnoreCase("second")) {
            return new Second();
        } else if (select.equalsIgnoreCase("merged")) {
            return new Merged();
        } else {
            throw new CreateException(
                    "An invalid input existings for 'select'. Select one of 'first', 'second' or 'merged'");
        }
    }

    public String getPrependString() {
        return prependString;
    }

    public void setPrependString(String prependString) {
        this.prependString = prependString;
    }

    public FeatureListProvider<FeatureInputSingleObject> getItem() {
        return item;
    }

    public void setItem(FeatureListProvider<FeatureInputSingleObject> item) {
        this.item = item;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }
}
