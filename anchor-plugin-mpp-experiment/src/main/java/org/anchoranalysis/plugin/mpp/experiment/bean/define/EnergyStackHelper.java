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

package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.mpp.io.output.EnergyStackWriter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EnergyStackHelper {

    private static final String IDENTIFIER = EnergyStackWriter.OUTPUT_ENERGY_STACK_DIRECTORY;

    // TODO make this more elegant in the design We make a special exception for writing our
    // energyStacks
    public static void writeEnergyStackDictionary(
            ImageInitialization initialization,
            Optional<String> energyDictionaryName,
            InputOutputContext context) {

        try {
            if (initialization.stacks().keys().contains(IDENTIFIER)) {

                Dictionary dictionary = extractDictionary(initialization, energyDictionaryName);

                EnergyStack energyStack = createEnergyStack(initialization, dictionary);
                new EnergyStackWriter(energyStack, context.getOutputter()).writeEnergyStack();
            }
        } catch (NamedProviderGetException e) {
            context.getLogger().errorReporter().recordError(EnergyStackHelper.class, e.summarize());
        } catch (OutputWriteFailedException e) {
            context.getLogger().errorReporter().recordError(EnergyStackHelper.class, e);
        }
    }

    private static EnergyStack createEnergyStack(
            ImageInitialization initialization, Dictionary dictionary)
            throws NamedProviderGetException {
        return new EnergyStack(initialization.stacks().getException(IDENTIFIER), dictionary);
    }

    private static Dictionary extractDictionary(
            ImageInitialization initialization, Optional<String> energyDictionaryName)
            throws NamedProviderGetException {
        return OptionalUtilities.flatMap(
                        energyDictionaryName,
                        name -> initialization.dictionaries().getOptional(name))
                .orElseGet(Dictionary::new);
    }
}
