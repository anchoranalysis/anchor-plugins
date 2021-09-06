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

package org.anchoranalysis.plugin.image.bean.mask.provider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.mask.Mask;

@NoArgsConstructor
public class Reference extends MaskProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";

    /**
     * If true the channel is duplicated after it is retrieved, to prevent overwriting exisiting
     * data This is a shortcut to avoid embedding beans in a ChannelProviderDuplicate
     */
    @BeanField @Getter @Setter private boolean duplicate = false;
    // END BEAN PROPERTIES

    private Mask mask;

    public Reference(String id) {
        this.id = id;
    }

    @Override
    public Mask get() throws ProvisionFailedException {
        if (mask == null) {
            mask = getMaskUncached();
        }
        return mask;
    }

    /** Gets the {@link Mask} ignoring any caching. */
    private Mask getMaskUncached() throws ProvisionFailedException {
        try {
            Mask maskForId = getInitialization().masks().getException(id);

            if (duplicate) {
                return maskForId.duplicate();
            } else {
                return maskForId;
            }
        } catch (NamedProviderGetException e) {
            throw new ProvisionFailedException(e);
        }
    }
}
