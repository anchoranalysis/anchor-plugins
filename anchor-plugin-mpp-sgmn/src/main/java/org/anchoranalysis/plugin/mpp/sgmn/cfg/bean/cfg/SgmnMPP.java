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

package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGSchemeCreator;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgWithNRGTotal;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.memory.MemoryUtilities;
import org.anchoranalysis.core.name.provider.NamedProvider;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenne;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedStacksSet;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.sgmn.bean.cfg.CfgSgmn;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.sgmn.optscheme.DualStack;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.bean.define.DefineOutputterMPPWithNrg;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.SgmnMPPState;


// Segments a channel with marked pointed processes
public class SgmnMPP extends CfgSgmn {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private OptScheme<CfgNRGPixelized, CfgNRGPixelized> optScheme;

    @BeanField @Getter @Setter private CfgGen cfgGen;

    @BeanField @Getter @Setter private NRGSchemeCreator nrgSchemeCreator;

    @BeanField @Getter @Setter private KernelProposer<CfgNRGPixelized> kernelProposer;

    @BeanField @Getter @Setter private FeedbackReceiverBean<CfgNRGPixelized> feedbackReceiver;

    @BeanField @Getter @Setter private DefineOutputterMPPWithNrg define;

    // For debugging, allows us to exit before optimization begins
    @BeanField @Getter @Setter private boolean exitBeforeOpt = false;

    /**
     * If TRUE uses a constant seed for the random-number-generator (useful for debugging) otherwise
     * seeds with system clock
     */
    @BeanField @Getter @Setter private boolean fixRandomSeed = false;
    // END BEAN PROPERTIES

    private NRGSchemeWithSharedFeatures nrgSchemeShared;

    @Override
    public SgmnMPPState createExperimentState() {
        return new SgmnMPPState(kernelProposer, define.getDefine());
    }

    // Do segmentation
    @Override
    public Cfg sgmn(
            NamedStacksSet stacks,
            NamedProvider<ObjectCollection> objects,
            Optional<KeyValueParams> keyValueParams,
            BoundIOContext context)
            throws SegmentationFailedException {
        ListUpdatableMarkSetCollection updatableMarkSetCollection =
                new ListUpdatableMarkSetCollection();

        try {
            MemoryUtilities.logMemoryUsage("Start of SgmnMPP:sgmn", context.getLogReporter());

            return define.processInput(
                    context,
                    Optional.of(stacks),
                    Optional.of(objects),
                    keyValueParams,
                    (mppInit, nrgStack) ->
                            sgmnAndWrite(
                                    mppInit,
                                    nrgStack,
                                    updatableMarkSetCollection,
                                    keyValueParams,
                                    context));

        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }

    private Cfg sgmnAndWrite(
            MPPInitParams mppInit,
            NRGStackWithParams nrgStack,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            Optional<KeyValueParams> keyValueParams,
            BoundIOContext context)
            throws OperationFailedException {
        try {
            init(mppInit, context.getLogger());

            SgmnMPPHelper.writeStacks(mppInit.getImage(), nrgStack, context);

            context.getLogReporter()
                    .log("Distinct number of probMap = " + updatableMarkSetCollection.numProbMap());

            // We initialize the feedback receiver
            feedbackReceiver.initRecursive(mppInit, context.getLogger());

            MemoryUtilities.logMemoryUsage("Before findOpt", context.getLogReporter());

            new UpdateMarkSet(mppInit, nrgStack, updatableMarkSetCollection, context.getLogger())
                    .apply();

            DualStack dualStack =
                    wrapWithBackground(nrgStack, mppInit.getImage().stacks());

            if (exitBeforeOpt) {
                return new Cfg();
            }

            maybeWriteGroupParams(keyValueParams, context.getOutputManager());

            OptSchemeContext initContext =
                    new OptSchemeContext(
                            "MPP Sgmn",
                            nrgSchemeShared,
                            dualStack,
                            new TriggerTerminationCondition(),
                            context,
                            new RandomNumberGeneratorMersenne(fixRandomSeed),
                            cfgGen);

            CfgWithNRGTotal cfgNRG = findOpt(updatableMarkSetCollection, initContext);
            return cfgNRG.getCfg().deepCopy();

        } catch (InitException | CreateException | SegmentationFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private void init(MPPInitParams soMPP, Logger logger) throws InitException {
        cfgGen.initRecursive(logger);

        nrgSchemeShared = SgmnMPPHelper.initNRG(nrgSchemeCreator, soMPP.getFeature(), logger);

        // The kernelProposers can change proposerSharedObjects
        SgmnMPPHelper.initKernelProposers(kernelProposer, cfgGen, soMPP, logger);
    }

    private CfgWithNRGTotal findOpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection, OptSchemeContext context)
            throws SegmentationFailedException {
        try {
            CfgNRG cfgNRG =
                    optScheme
                            .findOpt(
                                    kernelProposer,
                                    updatableMarkSetCollection,
                                    feedbackReceiver,
                                    context)
                            .getCfgNRG();

            SgmnMPPOutputter.outputResults(
                    cfgNRG,
                    context.getDualStack(),
                    nrgSchemeShared
                            .getNrgScheme()
                            .getRegionMap()
                            .membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE),
                    context.getLogger(),
                    context.getOutputManager());

            return cfgNRG.getCfgWithTotal();

        } catch (OptTerminatedEarlyException e) {
            throw new SegmentationFailedException("Optimization terminated early", e);
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException("Some operation failed", e);
        }
    }

    private DualStack wrapWithBackground(
            NRGStackWithParams nrgStack, NamedProviderStore<Stack> store) throws CreateException {
        DisplayStack background =
                BackgroundCreator.createBackground(store, getBackgroundStackName());
        return new DualStack(nrgStack, background);
    }

    private void maybeWriteGroupParams(
            Optional<KeyValueParams> keyValueParams, BoundOutputManagerRouteErrors outputManager) {
        if (keyValueParams.isPresent()) {
            outputManager
                    .getWriterCheckIfAllowed()
                    .write("groupParams", () -> new GroupParamsGenerator(keyValueParams.get()));
        }
    }
}
