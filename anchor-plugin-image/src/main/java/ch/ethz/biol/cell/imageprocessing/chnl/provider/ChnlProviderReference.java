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

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;

@NoArgsConstructor
public class ChnlProviderReference extends ChnlProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";

    /**
     * If true the channel is duplicated after it is retrieved, to prevent overwriting existing data
     * This is a shortcut to avoid embedding beans in a ChnlProviderDuplicate
     */
    @BeanField @Getter @Setter private boolean duplicate = false;
    // END BEAN PROPERTIES

    private Channel channel;

    public ChnlProviderReference(String id) {
        super();
        this.id = id;
    }

    @Override
    public Channel create() throws CreateException {
        if (channel == null) {
            channel = getMaybeDuplicate();
        }
        return channel;
    }

    private Channel getMaybeDuplicate() throws CreateException {
        try {
            Channel existing = getInitializationParameters().getChnlCollection().getException(id);

            if (duplicate) {
                return existing.duplicate();
            } else {
                return existing;
            }
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
    }
}
