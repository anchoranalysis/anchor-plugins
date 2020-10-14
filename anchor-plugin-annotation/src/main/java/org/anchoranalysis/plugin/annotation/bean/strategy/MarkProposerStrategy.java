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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.MultipleComparer;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.feature.bean.mark.MarkEvaluator;

/** Annotates each image with a mark */
public class MarkProposerStrategy extends SinglePathStrategy {

    // START BEAN PROPERTIES
    /**
     * Name of the {@link MarkProposer} to use.
     */
    @BeanField @Getter @Setter private String markProposer;

    /**
     * Name of the {@link PointsFitter} to use.
     */
    @BeanField @Getter @Setter private String pointsFitter;

    @BeanField @OptionalBean @Getter @Setter
    private DerivePath pathDefaultMarks;

    @BeanField @OptionalBean @Getter @Setter
    private DerivePath pathParams;

    /**
     * Additional background-stacks that are possible to use while annotating.
     * 
     * <p>These must have the same dimensions as the energy-stack.
     */
    @BeanField @OptionalBean @Getter @Setter
    private List<DerivePath> additionalBackgrounds = new ArrayList<>();

    @BeanField @OptionalBean @Getter @Setter private MultipleComparer multipleComparer;

    /** A mark-evaluator that is added to the GUI to support this annotation */
    @BeanField @Getter @Setter private NamedBean<MarkEvaluator> markEvaluator;
    // END BEAN PROPERTIES

    public Optional<DerivePath> paramsDeriver() {
        return Optional.ofNullable(pathParams);
    }

    public Optional<DerivePath> marksDeriver() {
        return Optional.ofNullable(pathDefaultMarks);
    }

    @Override
    public int weightWidthDescription() {
        return 1;
    }

    @Override
    public Optional<String> annotationLabelFor(ProvidesStackInput item) {
        return Optional.empty();
    }
}
