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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.io.imagej.convert.ConvertFromImagePlus;
import org.anchoranalysis.io.imagej.convert.ConvertToImagePlus;

public class BackgroundSubtractor extends WithRadiusBase {

    @Override
    protected Channel createFromChannel(Channel channel, int radius) throws CreateException {
        return subtractBackground(channel, radius, true);
    }

    public static Channel subtractBackground(Channel channel, int radius, boolean doPreSmooth) {
        ImagePlus imp = ConvertToImagePlus.from(channel);

        BackgroundSubtracter plugin = new BackgroundSubtracter();
        for (int z = 0; z < channel.dimensions().z(); z++) {
            plugin.rollingBallBackground(
                    imp.getStack().getProcessor(z + 1),
                    radius,
                    false,
                    false,
                    false,
                    doPreSmooth,
                    true);
        }

        return ConvertFromImagePlus.toChannel(imp, channel.dimensions().resolution());
    }
}