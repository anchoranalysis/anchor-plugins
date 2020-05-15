package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.output.NRGStackWriter;

class NRGStackHelper {

	private NRGStackHelper() {}
		
	// TODO make this more elegant in the design We make a special exception for writing our nrgStacks
	public static void writeNRGStackParams( ImageInitParams soImage, String nrgParamsName, BoundIOContext context ) {
		
		try {
			if (soImage.getStackCollection().keys().contains("nrgStack")) {
				
				KeyValueParams params = !nrgParamsName.isEmpty() ?
					soImage.getParams().getNamedKeyValueParamsCollection().getNull( nrgParamsName )
					: null;
					
				NRGStackWriter.writeNRGStack(
					new NRGStackWithParams(
						soImage.getStackCollection().getException("nrgStack"),
						params
					),
					context
				);
			}
		} catch (NamedProviderGetException e) {
			context.getLogger().getErrorReporter().recordError(
				NRGStackHelper.class,
				e.summarize()
			);
		}
	}
}
