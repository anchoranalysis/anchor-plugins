/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.format;

import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.chnl.ChannelGetter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;

class ConvertingChnlCollection implements ChannelGetter {

    private ChannelGetter source;
    private ChannelConverter<?> chnlConverter;
    private ConversionPolicy conversionPolicy;

    public ConvertingChnlCollection(
            ChannelGetter source,
            ChannelConverter<?> chnlConverter,
            ConversionPolicy changeExisting) {
        super();
        this.source = source;
        this.chnlConverter = chnlConverter;
        this.conversionPolicy = changeExisting;
    }

    @Override
    public boolean hasChannel(String chnlName) {
        return source.hasChannel(chnlName);
    }

    @Override
    public Channel getChannel(String chnlName, int t, ProgressReporter progressReporter)
            throws GetOperationFailedException {

        Channel chnl = source.getChannel(chnlName, t, progressReporter);
        return chnlConverter.convert(chnl, conversionPolicy);
    }
}
