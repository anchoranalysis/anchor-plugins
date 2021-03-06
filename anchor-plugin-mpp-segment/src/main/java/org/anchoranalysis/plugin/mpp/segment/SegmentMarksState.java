/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.segment;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.segment.bean.ExperimentState;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;

// State that only needs to be initialized once can be shared across many calls to the algoritm
@AllArgsConstructor
public class SegmentMarksState implements ExperimentState {

    private static final String OUTPUT_SERIALIZED = "kernelProposer";

    private static final String MANIFEST_FUNCTION = OUTPUT_SERIALIZED;

    private KernelProposer<VoxelizedMarksWithEnergy, UpdatableMarksList> kernelProposer;
    private Define define;

    @Override
    public void outputBeforeAnyTasksAreExecuted(Outputter outputter) {

        outputter
                .writerSelective()
                .write(
                        "define",
                        () -> new XStreamGenerator<Object>(Optional.of("define")),
                        () -> define);
    }

    // We just need any single kernel proposer to write out
    @Override
    public void outputAfterAllTasksAreExecuted(Outputter outputter) {
        outputter
                .writerSelective()
                .write(
                        OUTPUT_SERIALIZED,
                        () -> new XStreamGenerator<Object>(Optional.of(MANIFEST_FUNCTION)),
                        () -> kernelProposer);
    }
}
