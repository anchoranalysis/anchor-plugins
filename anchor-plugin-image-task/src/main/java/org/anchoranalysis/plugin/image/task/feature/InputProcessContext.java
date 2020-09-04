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
package org.anchoranalysis.plugin.image.task.feature;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.io.csv.writer.RowLabels;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * @author Owen Feehan
 * @param <S> row-source that is duplicated for each new thread (to prevent any concurrency issues)
 */
public class InputProcessContext<S> {

    private static final String OUTPUT_THUMBNAILS = "thumbnails";

    // START REQUIRED ARGUMENTS
    ExportFeatureResultsAdder adder;

    @Getter S rowSource;

    @Getter FeatureNameList featureNames;

    @Getter Optional<String> groupGeneratorName;

    @Getter BoundIOContext context;
    // END REQUIRED ARGUMENTS

    @Getter boolean thumbnailsEnabled;

    public InputProcessContext(
            ExportFeatureResultsAdder adder,
            S rowSource,
            FeatureNameList featureNames,
            Optional<String> groupGeneratorName,
            BoundIOContext context) {
        super();
        this.adder = adder;
        this.rowSource = rowSource;
        this.featureNames = featureNames;
        this.groupGeneratorName = groupGeneratorName;
        this.context = context;
        this.thumbnailsEnabled = areThumbnailsEnabled(context);
    }


    public Path getModelDirectory() {
        return context.getModelDirectory();
    }

    public Logger getLogger() {
        return context.getLogger();
    }

    public void addResultsFor(RowLabels labels, ResultsVectorWithThumbnail results) {
        adder.addResultsFor(labels, results);
    }

    private boolean areThumbnailsEnabled(BoundIOContext context) {
        return context.getOutputManager().isOutputAllowed(OUTPUT_THUMBNAILS);
    }
}
