/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.channel.map;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.channel.ChannelMapCreator;
import org.anchoranalysis.image.io.bean.channel.IndexedChannel;
import org.anchoranalysis.image.io.channel.input.ChannelMap;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

public class FromMetadata extends ChannelMapCreator {

    @Override
    public ChannelMap create(OpenedImageFile openedFile, Logger logger) throws CreateException {

        try {
            Optional<List<String>> names = openedFile.channelNames(logger);
            if (!names.isPresent()) {
                throw new CreateException(
                        "No channels names are associated with the opened image-file");
            }

            ChannelMap map = new ChannelMap();
            for (int index = 0; index < names.get().size(); index++) {
                map.add(new IndexedChannel(names.get().get(index), index));
            }
            return map;
        } catch (ImageIOException e) {
            throw new CreateException(e);
        }
    }
}
