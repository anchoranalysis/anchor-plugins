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
package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorderIgnore;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.stack.ProviderAsStack;
import org.anchoranalysis.image.inference.bean.segment.instance.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.inference.InferenceModel;

/**
 * Segments a stack into objects.
 *
 * @author Owen Feehan
 * @param <T> model-type in pool
 */
public class SegmentStack<T extends InferenceModel> extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    /** The stack to segment */
    @Getter @Setter @BeanField private ProviderAsStack stack;

    /** The segmentation procedure. */
    @Getter @Setter @BeanField private SegmentStackIntoObjectsPooled<T> segment;

    // END BEAN PROPERTIES

    @Override
    public ObjectCollection get() throws ProvisionFailedException {
        try {
            return segment.segment(stack.getAsStack(), ExecutionTimeRecorderIgnore.instance())
                    .getObjects()
                    .atInputScale()
                    .objects();
        } catch (SegmentationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }
}
