/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.image.ImageLabelAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.plugin.annotation.bean.label.AnnotationLabel;
import org.anchoranalysis.plugin.annotation.bean.label.GroupedAnnotationLabels;

/**
 * Assigns a single label per image, as a whole.
 *
 * @author Owen Feehan
 */
public class ImageLabelStrategy extends SinglePathStrategy {

    // START BEAN PROPERTIES
    /** List of {@link AnnotationLabel}s available for annotation. */
    @BeanField @Getter @Setter private List<AnnotationLabel> labels;

    /** The weight to use for the width of the description. */
    @BeanField @Getter @Setter private int weightWidthDescription;
    // END BEAN PROPERTIES

    @Override
    public Optional<String> annotationLabelFor(ProvidesStackInput item, OperationContext context)
            throws OperationFailedException {
        try {
            return ReadAnnotationFromFile.readCheckExists(pathFor(item), context)
                    .map(ImageLabelAnnotation::getLabel);
        } catch (InputReadFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Creates a {@link GroupedAnnotationLabels} from the current labels.
     *
     * <p>This is actually called twice during a typical opening of an annotation.
     * But overhead is minor (assuming not very many labels).
     *
     * @return a new {@link GroupedAnnotationLabels} instance.
     */
    public GroupedAnnotationLabels groupedLabels() {
        return new GroupedAnnotationLabels(labels);
    }

    @Override
    public int weightWidthDescription() {
        return weightWidthDescription;
    }
}