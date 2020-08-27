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

package org.anchoranalysis.plugin.mpp.sgmn.bean.marks;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
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
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedStacks;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.feature.bean.energy.scheme.EnergySchemeCreator;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithTotalEnergy;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergySchemeWithSharedFeatures;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.segment.bean.SegmentIntoMarks;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.segment.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.segment.bean.optscheme.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.segment.optscheme.DualStack;
import org.anchoranalysis.mpp.segment.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.segment.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.bean.define.DefineOutputterMPPWithEnergy;
import org.anchoranalysis.plugin.mpp.sgmn.SgmnMPPState;

// Segments a channel with marked pointed processes
public class SgmnMPP extends SegmentIntoMarks {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private OptScheme<VoxelizedMarksWithEnergy, VoxelizedMarksWithEnergy> optScheme;

    @BeanField @Getter @Setter private MarkWithIdentifierFactory markFactory;

    @BeanField @Getter @Setter private EnergySchemeCreator energySchemeCreator;

    @BeanField @Getter @Setter private KernelProposer<VoxelizedMarksWithEnergy> kernelProposer;

    @BeanField @Getter @Setter
    private FeedbackReceiverBean<VoxelizedMarksWithEnergy> feedbackReceiver;

    @BeanField @Getter @Setter private DefineOutputterMPPWithEnergy define;

    // For debugging, allows us to exit before optimization begins
    @BeanField @Getter @Setter private boolean exitBeforeOpt = false;

    /**
     * If TRUE uses a constant seed for the random-number-generator (useful for debugging) otherwise
     * seeds with system clock
     */
    @BeanField @Getter @Setter private boolean fixRandomSeed = false;
    // END BEAN PROPERTIES

    private EnergySchemeWithSharedFeatures energySchemeShared;

    @Override
    public SgmnMPPState createExperimentState() {
        return new SgmnMPPState(kernelProposer, define.getDefine());
    }

    // Do segmentation
    @Override
    public MarkCollection segment(
            NamedStacks stacks,
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
                    (mppInit, energyStack) ->
                            sgmnAndWrite(
                                    mppInit,
                                    energyStack,
                                    updatableMarkSetCollection,
                                    keyValueParams,
                                    context));

        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }

    private MarkCollection sgmnAndWrite(
            MPPInitParams mppInit,
            EnergyStack energyStack,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            Optional<KeyValueParams> keyValueParams,
            BoundIOContext context)
            throws OperationFailedException {
        try {
            init(mppInit, context.getLogger());

            SgmnMPPHelper.writeStacks(mppInit.getImage(), energyStack, context);

            context.getLogReporter()
                    .log("Distinct number of probMap = " + updatableMarkSetCollection.numProbMap());

            // We initialize the feedback receiver
            feedbackReceiver.initRecursive(mppInit, context.getLogger());

            MemoryUtilities.logMemoryUsage("Before findOpt", context.getLogReporter());

            new UpdateMarkSet(mppInit, energyStack, updatableMarkSetCollection, context.getLogger())
                    .apply();

            DualStack dualStack = wrapWithBackground(energyStack, mppInit.getImage().stacks());

            if (exitBeforeOpt) {
                return new MarkCollection();
            }

            maybeWriteGroupParams(keyValueParams, context.getOutputManager());

            OptSchemeContext initContext =
                    new OptSchemeContext(
                            "MPP Segmentation",
                            energySchemeShared,
                            dualStack,
                            new TriggerTerminationCondition(),
                            context,
                            new RandomNumberGeneratorMersenne(fixRandomSeed),
                            markFactory);

            MarksWithTotalEnergy marks = findOpt(updatableMarkSetCollection, initContext);
            return marks.getMarks().deepCopy();

        } catch (InitException | CreateException | SegmentationFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private void init(MPPInitParams soMPP, Logger logger) throws InitException {
        markFactory.initRecursive(logger);

        energySchemeShared =
                SgmnMPPHelper.initEnergy(energySchemeCreator, soMPP.getFeature(), logger);

        // The kernelProposers can change proposerSharedObjects
        SgmnMPPHelper.initKernelProposers(kernelProposer, markFactory, soMPP, logger);
    }

    private MarksWithTotalEnergy findOpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection, OptSchemeContext context)
            throws SegmentationFailedException {
        try {
            MarksWithEnergyBreakdown marks =
                    optScheme
                            .findOpt(
                                    kernelProposer,
                                    updatableMarkSetCollection,
                                    feedbackReceiver,
                                    context)
                            .getMarks();

            SgmnMPPOutputter.outputResults(
                    marks,
                    context.getDualStack(),
                    energySchemeShared
                            .getEnergyScheme()
                            .getRegionMap()
                            .membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE),
                    context.getLogger(),
                    context.getOutputManager());

            return marks.getMarksWithTotalEnergy();

        } catch (OptTerminatedEarlyException e) {
            throw new SegmentationFailedException("Optimization terminated early", e);
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException("Some operation failed", e);
        }
    }

    private DualStack wrapWithBackground(EnergyStack energyStack, NamedProviderStore<Stack> store)
            throws CreateException {
        DisplayStack background =
                BackgroundCreator.createBackground(store, getBackgroundStackName());
        return new DualStack(energyStack, background);
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
