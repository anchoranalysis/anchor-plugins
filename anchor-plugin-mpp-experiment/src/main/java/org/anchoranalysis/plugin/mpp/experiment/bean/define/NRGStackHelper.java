/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.output.NRGStackWriter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NRGStackHelper {

    // TODO make this more elegant in the design We make a special exception for writing our
    // nrgStacks
    public static void writeNRGStackParams(
            ImageInitParams soImage, Optional<String> nrgParamsName, BoundIOContext context) {

        try {
            if (soImage.getStackCollection().keys().contains("nrgStack")) {

                KeyValueParams params =
                        OptionalUtilities.flatMap(
                                        nrgParamsName,
                                        paramsName ->
                                                soImage.getParams()
                                                        .getNamedKeyValueParamsCollection()
                                                        .getOptional(paramsName))
                                .orElseGet(KeyValueParams::new);

                NRGStackWriter.writeNRGStack(
                        new NRGStackWithParams(
                                soImage.getStackCollection().getException("nrgStack"), params),
                        context);
            }
        } catch (NamedProviderGetException e) {
            context.getLogger().errorReporter().recordError(NRGStackHelper.class, e.summarize());
        }
    }
}
