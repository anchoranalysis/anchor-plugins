/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.anchor.plugin.quick.bean.input;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.io.bean.channel.map.ChannelEntry;
import org.anchoranalysis.image.io.bean.channel.map.ChannelMap;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.channel.map.FromEntries;
import org.anchoranalysis.plugin.io.bean.input.channel.NamedChannels;

/** Helps in creating NamedChannels */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NamedChannelsCreator {

    public static NamedChannels create(
            InputManager<FileInput> files,
            String mainChannelName,
            int mainChannelIndex,
            List<ChannelEntry> additionalChannels,
            RasterReader rasterReader)
            throws BeanMisconfiguredException {
        NamedChannels namedChannels = new NamedChannels();
        namedChannels.setChannelMap(
                createChannelMap(mainChannelName, mainChannelIndex, additionalChannels));
        namedChannels.setFileInput(files);
        namedChannels.setRasterReader(rasterReader);
        return namedChannels;
    }

    private static ChannelMap createChannelMap(
            String mainChannelName, int mainChannelIndex, List<ChannelEntry> additionalChannels)
            throws BeanMisconfiguredException {
        FromEntries define = new FromEntries();
        define.setList(listEntries(mainChannelName, mainChannelIndex, additionalChannels));
        return define;
    }

    private static List<ChannelEntry> listEntries(
            String mainChannelName, int mainChannelIndex, List<ChannelEntry> additionalChannels)
            throws BeanMisconfiguredException {
        List<ChannelEntry> out = new ArrayList<>();
        addChannelEntry(out, mainChannelName, mainChannelIndex);

        for (ChannelEntry entry : additionalChannels) {

            if (entry.getIndex() == mainChannelIndex) {
                throw new BeanMisconfiguredException(
                        String.format(
                                "Channel '%s' for index %d is already defined as the main channel. There cannot be an additional channel.",
                                mainChannelName, mainChannelIndex));
            }

            addChannelEntry(out, entry.getName(), entry.getIndex());
        }
        return out;
    }

    private static void addChannelEntry(List<ChannelEntry> list, String name, int index) {
        list.add(new ChannelEntry(name, index));
    }
}
