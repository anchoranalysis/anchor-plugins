/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.dim.provider;

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.stack.Stack;

/**
 * Creates image-dimensions by referencing them from a ChnlProvider
 *
 * <p>One of either chnlProvider or id must be set, but not both
 *
 * <p>id will look for a Chnl or a Stack in that order
 */
public class ImageDimProviderFromChnl extends ImageDimProvider {

    // START BEAN PROPERTIES
    @BeanField @AllowEmpty private String id = "";

    @BeanField @OptionalBean private ChnlProvider chnl;
    // END BEAN PROPERTIES

    @Override
    public void onInit(ImageInitParams so) throws InitException {
        super.onInit(so);
        if (id.isEmpty() && chnl == null) {
            throw new InitException("One of either chnlProvider or id must be set");
        }
        if (!id.isEmpty() && chnl != null) {
            throw new InitException("Only one -not both- of chnlProvider and id should be set");
        }
    }

    @Override
    public ImageDimensions create() throws CreateException {
        return selectChnl().getDimensions();
    }

    private Channel selectChnl() throws CreateException {

        if (!id.isEmpty()) {
            return selectChnlForId(id);
        }

        return chnl.create();
    }

    private Channel selectChnlForId(String id) throws CreateException {

        try {
            return OptionalUtilities.orFlat(
                            getInitializationParameters().getChnlCollection().getOptional(id),
                            getInitializationParameters()
                                    .getStackCollection()
                                    .getOptional(id)
                                    .map(ImageDimProviderFromChnl::firstChnl))
                    .orElseThrow(
                            () ->
                                    new CreateException(
                                            String.format(
                                                    "Failed to find either a channel or stack with id `%s`",
                                                    id)));

        } catch (NamedProviderGetException e) {
            throw new CreateException(
                    String.format("A error occurred while retrieving channel `%s`", id), e);
        }
    }

    private static Channel firstChnl(Stack stack) {
        return stack.getChnl(0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChnlProvider getChnl() {
        return chnl;
    }

    public void setChnl(ChnlProvider chnl) {
        this.chnl = chnl;
    }
}
