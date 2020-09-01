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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.mpp.feature.bean.mark.MarkEvaluator;

/** Annotates each image with a mark */
public class MarkProposerStrategy extends SingleFilePathGeneratorStrategy {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String markProposerName;

    @BeanField @Getter @Setter private String pointsFitterName;

    @BeanField @Getter @Setter private String markEvaluatorName;

    @BeanField @OptionalBean @Getter @Setter
    private FilePathGenerator defaultMarksFilePathGenerator;

    @BeanField @OptionalBean @Getter @Setter
    private FilePathGenerator keyValueParamsFilePathGenerator;

    @BeanField @OptionalBean @Getter @Setter
    private List<FilePathGenerator> listDisplayRasters = new ArrayList<>();

    @BeanField @OptionalBean @Getter @Setter private MultipleComparer multipleComparer;

    /** If-defined, mark-evaluator that is added to the GUI to support this annotation */
    @BeanField @OptionalBean @Getter @Setter private MarkEvaluator markEvaluator;
    // END BEAN PROPERTIES

    public Optional<FilePathGenerator> paramsFilePathGenerator() {
        return Optional.ofNullable(keyValueParamsFilePathGenerator);
    }

    public Optional<FilePathGenerator> marksFilePathGenerator() {
        return Optional.ofNullable(defaultMarksFilePathGenerator);
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
