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
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.bean.displayer.StackDisplayer;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.output.box.ScaleableBackground;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.spatial.scale.ScaleFactor;

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
    private VoxelsResizer resizer;
    private StackDisplayer displayer;

    public Optional<ScaleableBackground> determineBackground(Optional<Stack> backgroundSource)
            throws OperationFailedException {
        return OptionalUtilities.flatMap(backgroundSource, this::determineScaledBackground);
    }

    private Optional<ScaleableBackground> determineScaledBackground(Stack backgroundSource)
            throws OperationFailedException {
        try {
            return determineBackground(backgroundSource)
                    .map(stack -> ScaleableBackground.scaleBy(stack, scaleFactor, resizer));
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Derives a background-stack from a stack that is a source of possible backgrounds
     *
     * @throws CreateException
     */
    private Optional<DisplayStack> determineBackground(Stack backgroundSource)
            throws CreateException {

        if (backgroundChannelIndex > -1) {
            return Optional.of(extractChannelAsStack(backgroundSource, backgroundChannelIndex));
        }

        int numberChannels = backgroundSource.getNumberChannels();
        return switch (numberChannels) {
            case 0 -> Optional.empty();
            case 1, 3 -> Optional.of(displayer.deriveFrom(backgroundSource));
            default -> Optional.of(extractChannelAsStack(backgroundSource, 0));
        };
    }

    private DisplayStack extractChannelAsStack(Stack stack, int index) throws CreateException {
        return displayer.deriveFrom(stack.getChannel(index));
    }
}
