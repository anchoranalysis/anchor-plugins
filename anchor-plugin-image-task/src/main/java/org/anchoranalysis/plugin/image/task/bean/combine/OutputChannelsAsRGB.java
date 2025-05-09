/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.combine;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.RGBStack;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

/**
 * Write {@code channels} to the file-system as a single RGB image.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OutputChannelsAsRGB {

    /**
     * Whether the channels have appropriate names to output as RGB image.
     *
     * @param <T> element-type
     * @param namedElements elements each with a corresponding name.
     * @return a map of channel-names to elements, if RGB, otherwise {@link Optional#empty} if
     *     separately.
     */
    public static <T> Optional<Map<String, T>> canOutputAsRGB(
            Collection<Entry<String, T>> namedElements) {
        if (namedElements.size() == 3 || namedElements.size() == 4) {
            // Consider writing the channels together as an RGB stack rather than separately
            // If there is an alpha channel with the RGB, it is ignored.

            // Build a map between each channel-name and the aggregator, and check if the keys of
            // the map correspond
            // exactly to what we expect in RGB
            Map<String, T> channelsMap =
                    namedElements.stream()
                            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            if (RGBChannelNames.isValidNameSet(channelsMap.keySet(), false)
                    || RGBChannelNames.isValidNameSet(channelsMap.keySet(), true)) {
                return Optional.of(channelsMap);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Write the channels as a single RGB image to the file-system.
     *
     * @param channelSupplier returns a {@link Channel} when called with each of the three standard
     *     names in {@link RGBChannelNames}.
     * @param context where to write the image.
     * @param outputName the output-name to write them as.
     * @throws IOException if an I/O error occurs during writing.
     */
    public static void output(
            CheckedFunction<String, Channel, OperationFailedException> channelSupplier,
            InputOutputContext context,
            String outputName)
            throws IOException {
        try {
            RGBStack stack =
                    new RGBStack(
                            channelSupplier.apply(RGBChannelNames.RED),
                            channelSupplier.apply(RGBChannelNames.GREEN),
                            channelSupplier.apply(RGBChannelNames.BLUE));

            context.getOutputter()
                    .writerPermissive()
                    .write(outputName, () -> new StackGenerator(true), stack::asStack);

        } catch (OperationFailedException e) {
            throw new IOException(
                    "Unable to extract a particular color channel to output an aggregate as a RGB",
                    e);
        } catch (IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        }
    }
}
