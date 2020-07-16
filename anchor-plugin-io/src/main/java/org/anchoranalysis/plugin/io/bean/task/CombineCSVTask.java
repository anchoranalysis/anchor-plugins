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
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import lombok.Getter;
import lombok.Setter;

// At the moment, we don't check if the name number of rows/columns exist
public class CombineCSVTask extends Task<FileInput, CSVWriter> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String seperator = ",";

    @BeanField @Getter @Setter private boolean firstLineHeaders = true;

    @BeanField @Getter @Setter private boolean transposed = false;

    @BeanField @Getter @Setter private boolean addName = true;
    // END BEAN PROPERTIES

    @Override
    public CSVWriter beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {

        try {
            Optional<CSVWriter> writer =
                    CSVWriter.createFromOutputManager("featureReport", outputManager.getDelegate());

            if (!writer.isPresent()) {
                throw new ExperimentExecutionException(
                        "'featureReport' output not enabled, as is required");
            }

            return writer.get();

        } catch (AnchorIOException e) {
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
    public void doJobOnInputObject(InputBound<FileInput, CSVWriter> params)
            throws JobExecutionException {

        FileInput inputObject = params.getInputObject();
        CSVWriter writer = params.getSharedState();

        if (writer == null || !writer.isOutputEnabled()) {
            return;
        }

        Path inputPath = inputObject.getFile().toPath();
        try (ReadByLine readByLine = CSVReaderByLine.open(inputPath, seperator, firstLineHeaders)) {

            String name =
                    addName ? inputObject.descriptiveName() : null; // NULL means no-name is added
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
    public void afterAllJobsAreExecuted(CSVWriter writer, BoundIOContext context)
            throws ExperimentExecutionException {
        if (writer != null) {
            writer.close();
        }
    }
}
