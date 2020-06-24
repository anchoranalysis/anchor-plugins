package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;

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
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

public class NRGStackUtilities {

	private NRGStackUtilities() {}
	
	public static FeatureCalculatorSingle<FeatureInputSingleObj> maybeAddNrgStack(
		FeatureCalculatorSingle<FeatureInputSingleObj> session,
		Optional<Channel> chnl
	) throws CreateException {
		
		if (chnl.isPresent()) {
			return addNrgStack(
				session,
				chnl.get()
			);
		} else {
			return session;
		}
	}
	
	public static FeatureCalculatorSingle<FeatureInputSingleObj> addNrgStack(
		FeatureCalculatorSingle<FeatureInputSingleObj> session,
		Channel chnl
	) throws CreateException {
		
		// Make sure an NRG stack is added to each params that are called
		NRGStackWithParams nrgStack = new NRGStackWithParams(chnl); 
		return new FeatureCalculatorSingleChangeInput<>(
			session,
			params -> params.setNrgStack(nrgStack)
		);
	}
}
