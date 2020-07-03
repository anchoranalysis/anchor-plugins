package org.anchoranalysis.plugin.mpp.experiment.bean.define;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;

import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.output.NRGStackWriter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class NRGStackHelper {
		
	// TODO make this more elegant in the design We make a special exception for writing our nrgStacks
	public static void writeNRGStackParams( ImageInitParams soImage, Optional<String> nrgParamsName, BoundIOContext context ) {
		
		try {
			if (soImage.getStackCollection().keys().contains("nrgStack")) {
				
				KeyValueParams params = OptionalUtilities.flatMap(
					nrgParamsName,
					paramsName -> soImage.getParams().getNamedKeyValueParamsCollection().getOptional( paramsName )
				).orElseGet(KeyValueParams::new);
					
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
