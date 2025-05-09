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

package org.anchoranalysis.plugin.mpp.bean.define;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalFactory;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.bean.task.TaskWithoutSharedState;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.io.bean.object.feature.OutputFeatureTable;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.output.EnergyStackWriter;

/**
 * Derives various types of outputs (images, histograms etc.) from {@link MultiInput}s.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td rowspan="3"><i>outputs produced by a {@link DefineOutputter} in {@code define}</i></td></tr>
 * <tr><td rowspan="3"><i>outputs produced by a {@link OutputFeatureTable} in {@code featureTables}</i></td></tr>
 * <tr><td rowspan="3"><i>outputs produced by a {@link EnergyStackWriter}</i></td></tr>
 * <tr><td rowspan="3"><i>outputs from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class Develop extends TaskWithoutSharedState<MultiInput> {

    // START BEAN PROPERTIES
    /** Defines entities (chanels, stacks etc.) that are derived from inputs and other entities. */
    @BeanField @Getter @Setter private DefineOutputter define;

    /** Specifies a feature-table that can also be outputted. */
    @BeanField @Getter @Setter private List<OutputFeatureTable> featureTables = Arrays.asList();

    /**
     * If non-empty, the identifier for a dictionary that is treated as part of the energy-stack.
     */
    @BeanField @AllowEmpty @Getter @Setter private String dictionary = "";

    // END BEAN PROPERTIES

    @Override
    public void doJobOnInput(InputBound<MultiInput, NoSharedState> input)
            throws JobExecutionException {

        try {
            define.process(
                    input,
                    initialization ->
                            outputFeaturesAndEnergyStack(initialization, input.getContextJob()));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        OutputEnabledMutable outputs = super.defaultOutputs();
        define.addAllOutputNamesTo(outputs);
        outputs.addEnabledOutputFirst(OutputFeatureTable.OUTPUT_FEATURE_TABLE);
        return outputs;
    }

    private void outputFeaturesAndEnergyStack(
            ImageInitialization initialization, InputOutputContext context)
            throws OperationFailedException {

        try {
            outputFeatureTables(initialization, context);
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }

        EnergyStackHelper.writeEnergyStackDictionary(
                initialization, OptionalFactory.create(dictionary), context);
    }

    private void outputFeatureTables(ImageInitialization initialization, InputOutputContext context)
            throws IOException {
        for (OutputFeatureTable outputFeatureTable : featureTables) {

            try {
                outputFeatureTable.initializeRecursive(initialization, context.getLogger());
            } catch (InitializeException e) {
                throw new IOException(e);
            }
            outputFeatureTable.output(context);
        }
    }
}
