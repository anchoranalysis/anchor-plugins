/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.StringMap;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.labeller.ImageLabellerStringMapInitialization;

/**
 * Maps one set of labels to another
 *
 * @author Owen Feehan
 * @param <T> the init-param-type of filter that is the delegate
 */
public class ImageLabellerStringMap<T>
        extends ImageLabeller<ImageLabellerStringMapInitialization<T>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ImageLabeller<T> filter;

    @BeanField @Getter @Setter private StringMap map;
    // END BEAN PROPERTIES

    @Override
    public ImageLabellerStringMapInitialization<T> initialize(Path pathForBinding)
            throws InitializeException {
        return new ImageLabellerStringMapInitialization<>(
                map.create(), filter.initialize(pathForBinding));
    }

    @Override
    public Set<String> allLabels(ImageLabellerStringMapInitialization<T> initialization) {
        return new HashSet<>(initialization.getMap().values());
    }

    @Override
    public String labelFor(
            ImageLabellerStringMapInitialization<T> sharedState,
            ProvidesStackInput input,
            InputOutputContext context)
            throws OperationFailedException {
        String firstId = filter.labelFor(sharedState.getInitialization(), input, context);
        return sharedState.getMap().get(firstId);
    }
}
