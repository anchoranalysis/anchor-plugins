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

package org.anchoranalysis.plugin.image.bean.channel.provider.score;

import java.util.Optional;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.object.label.LabelObjects;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.channel.provider.UnaryWithObjectsBase;

/**
 * Assigns a unique id number to each object.
 *
 * <p>The behaviour is undefined when objects overlap with each other. An ID of either object
 * arbitrarily will be assigned.
 *
 * @author Owen Feehan
 */
public class IdentifyObjects extends UnaryWithObjectsBase {

    private static final LabelObjects<ObjectMask> LABELLER =
            new LabelObjects<>(object -> object, (object, back) -> object.shiftBackBy(back));

    @Override
    protected Channel createFromChannel(Channel channel, ObjectCollection objects)
            throws ProvisionFailedException {
        try {
            LABELLER.labelElements(channel, objects.asList(), Optional.empty());
        } catch (CreateException e) {
            throw new ProvisionFailedException(e);
        }
        return channel;
    }
}
