/*-
 * #%L
 * anchor-plugin-image
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
/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NRGStackUtilities {

    public static FeatureCalculatorSingle<FeatureInputSingleObject> maybeAddNrgStack(
            FeatureCalculatorSingle<FeatureInputSingleObject> session, Optional<Channel> chnl) {

        if (chnl.isPresent()) {
            return addNrgStack(session, chnl.get());
        } else {
            return session;
        }
    }

    public static FeatureCalculatorSingle<FeatureInputSingleObject> addNrgStack(
            FeatureCalculatorSingle<FeatureInputSingleObject> session, Channel chnl) {

        // Make sure an NRG stack is added to each params that are called
        NRGStackWithParams nrgStack = new NRGStackWithParams(chnl);
        return new FeatureCalculatorSingleChangeInput<>(
                session, params -> params.setNrgStack(nrgStack));
    }
}
