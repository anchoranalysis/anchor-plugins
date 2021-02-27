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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.identifier.provider.NamedProvider;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.feature.shared.FeaturesInitialization;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.StackIdentifiers;
import org.anchoranalysis.mpp.bean.init.MarksInitialization;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.feature.bean.energy.scheme.EnergySchemeCreator;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergyScheme;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergySchemeWithSharedFeatures;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SegmentHelper {

    public static EnergySchemeWithSharedFeatures initEnergy(
            EnergySchemeCreator energySchemeCreator,
            FeaturesInitialization featureInit,
            Logger logger)
            throws InitException {

        energySchemeCreator.initRecursive(featureInit, logger);

        try {
            EnergyScheme energyScheme = energySchemeCreator.create();

            return new EnergySchemeWithSharedFeatures(
                    energyScheme, featureInit.getSharedFeatures(), logger);
        } catch (CreateException e) {
            throw new InitException(e);
        }
    }

    public static EnergyStack createEnergyStack(
            NamedProvider<Stack> stackCollection, Dictionary dictionary) throws CreateException {
        try {
            EnergyStackWithoutParams energyStack =
                    new EnergyStackWithoutParams(
                            stackCollection.getException(StackIdentifiers.ENERGY_STACK));
            return new EnergyStack(energyStack, dictionary);
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
    }

    public static void initKernelProposers(
            KernelProposer<VoxelizedMarksWithEnergy, UpdatableMarksList> kernelProposer,
            MarkWithIdentifierFactory markFactory,
            MarksInitialization initialization,
            Logger logger)
            throws InitException {
        // The initial initiation to establish the kernelProposer
        kernelProposer.init();
        kernelProposer.initWithProposerSharedObjects(initialization, logger);

        // Check that the kernelProposer is compatible with our marks
        kernelProposer.checkCompatibleWith(markFactory.getTemplateMark().create());
    }
}
