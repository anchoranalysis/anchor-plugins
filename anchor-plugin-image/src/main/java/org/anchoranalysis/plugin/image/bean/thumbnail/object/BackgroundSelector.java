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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.generator.raster.boundingbox.ScaleableBackground;
import org.anchoranalysis.image.voxel.interpolator.Interpolator;
import org.anchoranalysis.spatial.extent.scale.ScaleFactor;

/**
 * Selects a background from an optional stack with an unknown number of channels, and assigns a
 * scale-factor
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class BackgroundSelector {

    private int backgroundChannelIndex;
    private ScaleFactor scaleFactor;
    private Interpolator interpolator;

    public Optional<ScaleableBackground> determineBackground(Optional<Stack> backgroundSource) {
        return backgroundSource.flatMap(this::determineScaledBackground);
    }

    private Optional<ScaleableBackground> determineScaledBackground(Stack backgroundSource) {
        return determineBackground(backgroundSource)
                .map(stack -> ScaleableBackground.scaleBy(stack, scaleFactor, interpolator));
    }

    /** Derives a background-stack from a stack that is a source of possible backgrounds */
    private Optional<Stack> determineBackground(Stack backgroundSource) {

        if (backgroundChannelIndex > -1) {
            return Optional.of(extractChannelAsStack(backgroundSource, backgroundChannelIndex));
        }

        int numberChannels = backgroundSource.getNumberChannels();
        if (numberChannels == 0) {
            return Optional.empty();
        } else if (numberChannels == 3 || numberChannels == 1) {
            return Optional.of(backgroundSource);
        } else {
            return Optional.of(extractChannelAsStack(backgroundSource, 0));
        }
    }

    private static Stack extractChannelAsStack(Stack stack, int index) {
        return new Stack(stack.getChannel(index));
    }
}
