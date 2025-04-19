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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.minima;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Abstract base class for imposing minima on a channel for watershed segmentation.
 *
 * <p>This class provides a framework for different strategies of imposing minima on a channel,
 * which is a crucial step in watershed-based segmentation algorithms.
 */
public abstract class MinimaImposition extends AnchorBean<MinimaImposition> {

    /**
     * Imposes minima on a channel based on seed objects and an optional containing mask.
     *
     * @param channel the {@link Channel} on which to impose minima
     * @param seeds the {@link ObjectCollection} representing seed objects for minima
     * @param containingMask an optional {@link ObjectMask} that constrains the area where minima
     *     can be imposed
     * @return a new {@link Channel} with imposed minima
     * @throws OperationFailedException if the minima imposition operation fails
     */
    public abstract Channel imposeMinima(
            Channel channel, ObjectCollection seeds, Optional<ObjectMask> containingMask)
            throws OperationFailedException;
}
