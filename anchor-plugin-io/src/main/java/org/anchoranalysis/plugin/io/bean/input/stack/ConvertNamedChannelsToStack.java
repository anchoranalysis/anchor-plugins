/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.input.stack;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;

/**
 * Manager that converts (one channel) {@link NamedChannelsInput} to {@link StackSequenceInput}.
 *
 * @author Owen Feehan
 */
public class ConvertNamedChannelsToStack extends InputManager<StackSequenceInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private InputManager<NamedChannelsInput> input;

    /**
     * By default all channels are converted into a stack. If non-empty, only this channel is
     * converted into a stack.
     */
    @BeanField @Getter @Setter @AllowEmpty private String channelName;

    @BeanField @Getter @Setter private int timeIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public InputsWithDirectory<StackSequenceInput> inputs(InputManagerParams params)
            throws InputReadFailedException {
        return input.inputs(params).map(this::convert);
    }

    private StackSequenceInput convert(NamedChannelsInput input) {
        return new ConvertNamedChannelsInputToStack(
                input, timeIndex, OptionalFactory.create(channelName));
    }
}
