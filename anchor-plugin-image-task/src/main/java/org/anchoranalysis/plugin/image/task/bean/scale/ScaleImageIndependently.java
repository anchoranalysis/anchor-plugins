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

package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.List;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.output.outputter.Outputter;

/**
 * Creates a scaled copy of images, treating each image independently, and without any padding or
 * alignment.
 *
 * <p>When given input images of identical size, it will produce outputs of identical (scaled) size.
 *
 * <p>However, when given inputs of varying size, it will usually produce outputs of varying
 * (scaled) size.
 *
 * @author Owen Feehan
 */
public class ScaleImageIndependently extends ScaleImage<NoSharedState> {

    @Override
    public NoSharedState beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<StackSequenceInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {
        return NoSharedState.INSTANCE;
    }
}
