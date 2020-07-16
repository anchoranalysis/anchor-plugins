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
/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.StringMap;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.labeller.ImageLabellerStringMapInitParams;

/**
 * Maps one set of labels to another
 *
 * @author Owen Feehan
 * @param T the init-param-type of filter that is the delegate
 */
public class ImageLabellerStringMap<T> extends ImageLabeller<ImageLabellerStringMapInitParams<T>> {

    // START BEAN PROPERTIES
    @BeanField private ImageLabeller<T> filter;

    @BeanField private StringMap map;
    // END BEAN PROPERTIES

    @Override
    public ImageLabellerStringMapInitParams<T> init(Path pathForBinding) throws InitException {
        return new ImageLabellerStringMapInitParams<>(map.create(), filter.init(pathForBinding));
    }

    @Override
    public Set<String> allLabels(ImageLabellerStringMapInitParams<T> params) {
        return new HashSet<>(params.getMap().values());
    }

    @Override
    public String labelFor(
            ImageLabellerStringMapInitParams<T> sharedState,
            ProvidesStackInput input,
            BoundIOContext context)
            throws OperationFailedException {
        String firstId = filter.labelFor(sharedState.getInitParams(), input, context);
        return sharedState.getMap().get(firstId);
    }

    public StringMap getMap() {
        return map;
    }

    public void setMap(StringMap map) {
        this.map = map;
    }

    public ImageLabeller<T> getFilter() {
        return filter;
    }

    public void setFilter(ImageLabeller<T> filter) {
        this.filter = filter;
    }
}
