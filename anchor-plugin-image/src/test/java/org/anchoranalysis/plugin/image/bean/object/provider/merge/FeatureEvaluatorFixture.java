package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.nio.file.Path;

/*-
 * #%L
 * anchor-plugin-image
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorNrgStack;
import org.anchoranalysis.plugin.image.test.ProviderFixture;

class FeatureEvaluatorFixture {
	
	private FeatureEvaluatorFixture() {}
	
	public static <T extends FeatureInput> FeatureEvaluatorNrgStack<T> createNrg(
		Feature<T> feature,
		Logger logger,
		Path modelDirectory
	) throws CreateException {
		FeatureEvaluatorNrgStack<T> eval = new FeatureEvaluatorNrgStack<>();
		eval.setFeatureProvider(
			ProviderFixture.providerFor( feature )
		);
		try {
			eval.init(
				SharedFeaturesInitParams.create(logger, modelDirectory),
				logger
			);
		} catch (InitException e) {
			throw new CreateException(e);
		}
		return eval;
	}
}