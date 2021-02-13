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
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.FunctionalIterate;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.channel.ChannelEntry;
import org.anchoranalysis.image.io.bean.channel.ChannelMap;
import org.anchoranalysis.image.io.channel.input.NamedEntries;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

/**
 * Names of the channels from the metadata if it exists, or after RGB, or by index
 *
 * <p>Naming rules - in order of priority:
 *
 * <ol>
 *   <li>The channel name from the metadata
 *   <li>red, green or blue if it's RGB
 *   <li>channel-%d where %d is the index of the channel
 * </ol>
 *
 * @author Owen Feehan
 */
public class Autoname extends ChannelMap {

    private static final String[] RGB_CHANNEL_NAMES = RGBChannelNames.asArray();

    @Override
    public NamedEntries createMap(OpenedImageFile openedFile) throws CreateException {

        NamedEntries map = new NamedEntries();

        try {
            Optional<List<String>> names = openedFile.channelNames();

            boolean rgb = openedFile.isRGB() && openedFile.numberChannels() == 3;

            // The insertion order is critical here to remember R, G, B
            FunctionalIterate.repeatWithIndex(
                    openedFile.numberChannels(),
                    channelIndex -> addEntryToMap(map, names, rgb, channelIndex));

        } catch (ImageIOException e) {
            throw new CreateException(e);
        }

        return map;
    }

    private static void addEntryToMap(
            NamedEntries map, Optional<List<String>> names, boolean rgb, int channelIndex) {
        String entryName = nameFor(channelIndex, names, rgb);
        map.add(new ChannelEntry(entryName, channelIndex));
    }

    private static String nameFor(int channelIndex, Optional<List<String>> names, boolean rgb) {
        if (names.isPresent()) {
            return names.get().get(channelIndex);
        } else if (rgb) {
            return rgbNameFor(channelIndex);
        } else {
            return String.format("channel-%d", channelIndex);
        }
    }

    private static String rgbNameFor(int channelIndex) {
        if (channelIndex < 3) {
            return RGB_CHANNEL_NAMES[channelIndex];
        } else {
            throw new AnchorImpossibleSituationException();
        }
    }
}
