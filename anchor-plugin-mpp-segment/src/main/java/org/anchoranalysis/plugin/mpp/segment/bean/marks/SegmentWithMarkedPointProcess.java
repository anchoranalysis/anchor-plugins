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

package org.anchoranalysis.plugin.mpp.segment.bean.marks;

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
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.NamedStacks;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.feature.bean.energy.scheme.EnergySchemeCreator;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithTotalEnergy;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergySchemeWithSharedFeatures;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.io.output.BackgroundCreator;
import org.anchoranalysis.mpp.io.output.EnergyStackWriter;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.segment.bean.SegmentIntoMarks;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optimization.OptimizationScheme;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TriggerTerminationCondition;
import org.anchoranalysis.mpp.segment.optimization.DualStack;
import org.anchoranalysis.mpp.segment.optimization.OptimizationContext;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.plugin.mpp.bean.define.DefineOutputterMPPWithEnergy;
import org.anchoranalysis.plugin.mpp.segment.SgmnMPPState;

/**
 * Segments a channel with marked pointed processes.
 * 
 * <p>Different operations occur on a set of marks (a <i>configuration</i>), with
 * each operation termed a <i>kernel</i>. These operations can, for example:
 * <ul>
 * <li>create a new mark(s) (<i>birth</i>)
 * <li>delete an existing mark(s) (<i>death</i>)
 * <li>modify an existing mark(s).
 * </ul>
 * 
 * <p>Each set of marks has an associated energy, a score determining how desirable
 * it is for the optimization routine. Certain scores are retained according to
 * an optimization-scheme, either deterministically or stochastically, either greedily
 * or with some non-greedy steps.
 *
 * @author Owen Feehan
 */
public class SegmentWithMarkedPointProcess extends SegmentIntoMarks {

    // START BEAN PROPERTIES
    /** 
     * The optimization-scheme that determines which configurations are accepted/rejected from one iteration to the next.
     */
    @BeanField @Getter @Setter
    private OptimizationScheme<VoxelizedMarksWithEnergy, VoxelizedMarksWithEnergy> optimization;

    /** 
     * Creates a new mark, before perhaps further manipulations by a kernel.
     */
    @BeanField @Getter @Setter private MarkWithIdentifierFactory markFactory;

    /** 
     * Creates an energy scheme that assigns an energy score to each collection of marks.
     */
    @BeanField @Getter @Setter private EnergySchemeCreator energySchemeCreator;

    /** 
     * Proposes kernel-changes during iterations of hte marked-point-processes.
     */
    @BeanField @Getter @Setter private KernelProposer<VoxelizedMarksWithEnergy> kernelProposer;

    /** 
     * Processes feedback from the segmentation algorithm for outputting / debugging.
     */
    @BeanField @Getter @Setter
    private FeedbackReceiverBean<VoxelizedMarksWithEnergy> feedbackReceiver;

    /** 
     * Adds definitions of stacks/objects etc. to be used during segmentation.
     */
    @BeanField @Getter @Setter private DefineOutputterMPPWithEnergy define;

    /**
     * Iff true the algorithm exits before optimization begins (useful for debugging).
     */
    @BeanField @Getter @Setter private boolean exitBeforeOptimization = false;

    /**
     * Iff true uses a constant seed for the random-number-generator (useful for debugging).
     *
     * <p>Otherwise, the random-number-generator seeds from the system clock.
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
            InputOutputContext context)
            throws SegmentationFailedException {
        ListUpdatableMarkSetCollection updatableMarkSetCollection =
                new ListUpdatableMarkSetCollection();

        try {
            MemoryUtilities.logMemoryUsage("Start of segment", context.getMessageReporter());

            return define.processInput(
                    context,
                    Optional.of(stacks),
                    Optional.of(objects),
                    keyValueParams,
                    (mppInit, energyStack) ->
                            segmentAndWrite(
                                    mppInit,
                                    energyStack,
                                    updatableMarkSetCollection,
                                    keyValueParams,
                                    context));

        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst( MarksOutputter.OUTPUT_OUTLINE_THIN, MarksOutputter.OUTPUT_MARKS_XML_SERIALIZED );
    }
    
    private MarkCollection segmentAndWrite(
            MPPInitParams mppInit,
            EnergyStack energyStack,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            Optional<KeyValueParams> keyValueParams,
            InputOutputContext context)
            throws OperationFailedException {
        try {
            init(mppInit, context.getLogger());

            new EnergyStackWriter(energyStack, context.getOutputter()).writeEnergyStack();

            context.getMessageReporter()
                    .log("Distinct number of probMap = " + updatableMarkSetCollection.numProbMap());

            // We initialize the feedback receiver
            feedbackReceiver.initRecursive(mppInit, context.getLogger());

            MemoryUtilities.logMemoryUsage("Before findOpt", context.getMessageReporter());

            new UpdateMarkSet(mppInit, energyStack, updatableMarkSetCollection, context.getLogger())
                    .apply();

            DualStack dualStack = wrapWithBackground(energyStack, mppInit.getImage().stacks());

            if (exitBeforeOptimization) {
                return new MarkCollection();
            }

            maybeWriteGroupParams(keyValueParams, context.getOutputter());

            OptimizationContext initContext =
                    new OptimizationContext(
                            "MPP Segmentation",
                            energySchemeShared,
                            dualStack,
                            new TriggerTerminationCondition(),
                            context,
                            new RandomNumberGeneratorMersenne(fixRandomSeed),
                            markFactory);

            MarksWithTotalEnergy marks = findOptimum(updatableMarkSetCollection, initContext);
            return marks.getMarks().deepCopy();

        } catch (InitException | CreateException | SegmentationFailedException | OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private void init(MPPInitParams soMPP, Logger logger) throws InitException {
        markFactory.initRecursive(logger);

        energySchemeShared =
                SegmentHelper.initEnergy(energySchemeCreator, soMPP.getFeature(), logger);

        // The kernelProposers can change proposerSharedObjects
        SegmentHelper.initKernelProposers(kernelProposer, markFactory, soMPP, logger);
    }

    private MarksWithTotalEnergy findOptimum(
            ListUpdatableMarkSetCollection updatableMarkSetCollection, OptimizationContext context)
            throws SegmentationFailedException {
        try {
            MarksWithEnergyBreakdown marks =
                    optimization
                            .findOptimum(
                                    kernelProposer,
                                    updatableMarkSetCollection,
                                    feedbackReceiver,
                                    context)
                            .getMarks();

            MarksOutputter.outputResults(
                    marks,
                    context.getDualStack(),
                    energySchemeShared
                            .getEnergyScheme()
                            .getRegionMap()
                            .membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE),
                    context.getLogger(),
                    context.getOutputter());

            return marks.getMarksWithTotalEnergy();

        } catch (OptimizationTerminatedEarlyException e) {
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
            Optional<KeyValueParams> keyValueParams, Outputter outputter) {
        if (keyValueParams.isPresent()) {
            outputter
                    .writerSelective()
                    .write("groupParams", GroupParamsGenerator::new, keyValueParams::get);
        }
    }
}
