/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.task;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.input.csv.CSVReaderByLine;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.io.input.csv.ReadByLine;
import org.anchoranalysis.io.input.file.FileInput;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;

/**
 * Combines multiple CSV files into a single CSV file.
 *
 * <p>No check occurs that the same number of rows/columns exist in the files being combined.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>combined</td><td>yes</td><td>A CSV combining the input CSV files.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class CombineCSV extends Task<FileInput, CSVWriter> {

    private static final String OUTPUT_COMBINED = "combined";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String seperator = ",";

    @BeanField @Getter @Setter private boolean firstLineHeaders = true;

    @BeanField @Getter @Setter private boolean transposed = false;

    @BeanField @Getter @Setter private boolean addName = true;
    // END BEAN PROPERTIES

    @Override
    public CSVWriter beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<FileInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {

        try {
            Optional<CSVWriter> writer =
                    CSVWriter.createFromOutputter(OUTPUT_COMBINED, outputter.getChecked());

            if (!writer.isPresent()) {
                throw new ExperimentExecutionException(
                        "'featureReport' output not enabled, as is required");
            }

            return writer.get();

        } catch (OutputWriteFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(FileInput.class);
    }

    @Override
    public void doJobOnInput(InputBound<FileInput, CSVWriter> parameters)
            throws JobExecutionException {

        FileInput fileInput = parameters.getInput();
        CSVWriter writer = parameters.getSharedState();

        if (writer == null || !writer.isOutputEnabled()) {
            return;
        }

        Path inputPath = fileInput.getFile().toPath();
        try (ReadByLine readByLine = CSVReaderByLine.open(inputPath, seperator, firstLineHeaders)) {

            String name = addName ? fileInput.identifier() : null; // null means no-name is added
            AddWithName addWithName = new AddWithName(writer, firstLineHeaders, name);

            if (transposed) {
                addWithName.addTransposed(readByLine);
            } else {
                addWithName.addNonTransposed(readByLine);
            }

        } catch (CSVReaderException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(CSVWriter writer, InputOutputContext context)
            throws ExperimentExecutionException {
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_COMBINED);
    }
}
