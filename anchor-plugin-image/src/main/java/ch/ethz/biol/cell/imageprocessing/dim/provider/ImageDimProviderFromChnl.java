/*-
 * #%L
 * anchor-plugin-image
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
