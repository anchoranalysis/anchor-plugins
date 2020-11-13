/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.mpp.experiment.bean.feature.report.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bean.ReportFeature;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import lombok.Getter;
import lombok.Setter;

/**
 * A base class for tasks that calculate and export {@link ReportFeature}s.
 * 
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>featureReport</td><td>yes</td><td>A CSV file with the report-features as columns (and an ID column), one for each input.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 * 
 * @author Owen Feehan
 * 
 * @param <T> input-object type
 * @param <S> shared-state type
 * @param <U> feature-type
 */
public abstract class ExportReportFeatures<T extends InputFromManager,S,U> extends Task<T,S> {

    private static final String OUTPUT_REPORT = "featureReport";
    
    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private List<ReportFeature<U>> listReportFeatures = new ArrayList<>();
    // END BEAN PROPERTIES
    
    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_REPORT);
    }
    
    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
    
    protected List<String> headerNames(Optional<String> prefixColumnName) {
        List<String> headerNames = ReportFeatureUtilities.headerNames(listReportFeatures);
        prefixColumnName.ifPresent(headerNames::add);
        return headerNames;
    }
    
    protected void writeFeaturesIntoReporter(
            U featureParam, CSVWriter writer, Optional<String> prefixValue, Logger logger) {
        List<TypedValue> rowElements =
                ReportFeatureUtilities.elementList(listReportFeatures, featureParam, logger);

        if (prefixValue.isPresent()) {
            rowElements.add(0, new TypedValue(prefixValue.get(), false));
        }

        writer.writeRow(rowElements);
    }
    
    protected Optional<CSVWriter> createWriter(OutputterChecked outputter) throws OutputWriteFailedException {
        return CSVWriter.createFromOutputter(
                OUTPUT_REPORT, outputter);
    }
}
