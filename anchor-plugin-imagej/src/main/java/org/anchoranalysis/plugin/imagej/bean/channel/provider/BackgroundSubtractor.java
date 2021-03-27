/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.channel.provider;

import ij.ImagePlus;
import ij.plugin.filter.BackgroundSubtracter;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.io.imagej.convert.ConvertFromImagePlus;
import org.anchoranalysis.io.imagej.convert.ConvertToImagePlus;
import org.anchoranalysis.io.imagej.convert.ImageJConversionException;
import org.anchoranalysis.spatial.Extent;

public class BackgroundSubtractor extends WithRadiusBase {

    @Override
    protected Channel createFromChannel(Channel channel, int radius) throws CreateException {
        try {
            ImagePlus image = ConvertToImagePlus.from(channel);
            subtractBackground(image, channel.extent(), radius);
            return ConvertFromImagePlus.toChannel(image, channel.resolution());
        } catch (ImageJConversionException e) {
            throw new CreateException(e);
        }
    }

    private void subtractBackground(ImagePlus image, Extent extent, int radius) {

        BackgroundSubtracter plugin = new BackgroundSubtracter();
        extent.iterateOverZ(
                z ->
                        plugin.rollingBallBackground(
                                image.getStack().getProcessor(z + 1),
                                radius,
                                false,
                                false,
                                false,
                                true,
                                true));
    }
}
