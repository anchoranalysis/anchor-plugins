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

package org.anchoranalysis.plugin.image.bean.channel.provider.assign;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;

/**
 * Assigns a value from a dictionary to all voxels in a channel.
 *
 * <p>This class extends {@link ChannelProviderUnary} to create a new channel by assigning
 * a single value, retrieved from a dictionary, to all voxels in the input channel.</p>
 */
public class AssignFromDictionary extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    /** 
     * The dictionary from which an assignment will occur.
     */
    @BeanField @Getter @Setter private DictionaryProvider dictionary;

    /** 
     * The key of the value in the dictionary that will be assigned.
     */
    @BeanField @Getter @Setter private String key;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws ProvisionFailedException {

        Dictionary createdDictionary = dictionary.get();

        if (!createdDictionary.containsKey(key)) {
            throw new ProvisionFailedException(String.format("Cannot find key '%s'", key));
        }

        int valueToAssign = (int) createdDictionary.getAsDouble(key);
        channel.voxels().assignValue(valueToAssign).toAll();
        return channel;
    }
}