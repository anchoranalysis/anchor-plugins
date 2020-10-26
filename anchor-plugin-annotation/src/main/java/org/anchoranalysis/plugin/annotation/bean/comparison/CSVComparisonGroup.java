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

package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.Optional;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroupList;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class CSVComparisonGroup<T extends Assignment> {
    
    private final AnnotationGroupList<T> annotationGroupList;
    
    /** What output-name to use for the CSV written. */ 
    private final String outputName;

    private void writeGroupStatsForGroup(AnnotationGroup<T> annotationGroup, CSVWriter writer) {
        writer.writeRow(annotationGroup.createValues());
    }

    public void writeGroupStats(Outputter outputter) throws OutputWriteFailedException {

        Optional<CSVWriter> writer =
                CSVWriter.createFromOutputter(outputName, outputter.getChecked());

        if (!writer.isPresent()) {
            return;
        }

        try {
            writer.get().writeHeaders(annotationGroupList.first().createHeaders());

            for (AnnotationGroup<T> group : annotationGroupList) {
                writeGroupStatsForGroup(group, writer.get());
            }

        } finally {
            writer.get().close();
        }
    }
}
