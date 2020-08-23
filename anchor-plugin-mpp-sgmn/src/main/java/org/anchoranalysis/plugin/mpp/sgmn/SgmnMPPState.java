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

package org.anchoranalysis.plugin.mpp.sgmn;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.sgmn.bean.ExperimentState;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;

// State that only needs to be initialized once can be shared across many calls to the algoritm
public class SgmnMPPState implements ExperimentState {

    private KernelProposer<VoxelizedMarksWithEnergy> kernelProposer;
    private Define define;

    public SgmnMPPState(KernelProposer<VoxelizedMarksWithEnergy> kernelProposer, Define define) {
        super();
        this.kernelProposer = kernelProposer;
        this.define = define;
    }

    @Override
    public void outputBeforeAnyTasksAreExecuted(BoundOutputManagerRouteErrors outputManager) {

        outputManager
                .getWriterCheckIfAllowed()
                .write("define", () -> new XStreamGenerator<Object>(define, Optional.of("define")));
    }

    // We just need any single kernel proposer to write out
    @Override
    public void outputAfterAllTasksAreExecuted(BoundOutputManagerRouteErrors outputManager) {
        outputManager
                .getWriterCheckIfAllowed()
                .write(
                        "kernelProposer",
                        () ->
                                new XStreamGenerator<Object>(
                                        kernelProposer, Optional.of("kernelProposer")));
    }
}
