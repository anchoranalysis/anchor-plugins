/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.sgmn.bean.cfg.ExperimentState;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;

// State that only needs to be initialized once can be shared across many calls to the algoritm
public class SgmnMPPState implements ExperimentState {

    private KernelProposer<CfgNRGPixelized> kernelProposer;
    private Define define;

    public SgmnMPPState(KernelProposer<CfgNRGPixelized> kernelProposer, Define define) {
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
