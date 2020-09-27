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
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.mpp.io.output.EnergyStackWriter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EnergyStackHelper {

    // TODO make this more elegant in the design We make a special exception for writing our
    // energyStacks
    public static void writeEnergyStackParams(
            ImageInitParams soImage, Optional<String> energyParamsName, InputOutputContext context) {

        try {
            if (soImage.stacks().keys().contains("energyStack")) {

                KeyValueParams params =
                        OptionalUtilities.flatMap(
                                        energyParamsName,
                                        paramsName ->
                                                soImage.params()
                                                        .getNamedKeyValueParamsCollection()
                                                        .getOptional(paramsName))
                                .orElseGet(KeyValueParams::new);

                EnergyStackWriter.writeEnergyStack(
                        new EnergyStack(soImage.stacks().getException("energyStack"), params),
                        context);
            }
        } catch (NamedProviderGetException e) {
            context.getLogger().errorReporter().recordError(EnergyStackHelper.class, e.summarize());
        }
    }
}
