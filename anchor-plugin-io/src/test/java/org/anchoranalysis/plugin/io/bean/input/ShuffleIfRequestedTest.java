/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2025 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.input;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.io.input.InputContextParameters;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.Test;

class ShuffleIfRequestedTest {

    @Test
    void testShuffleWhenRequested() throws InputReadFailedException {
        ShuffleIfRequested<InputFromManager> shuffler = new ShuffleIfRequested<>();

        List<InputFromManager> inputList = createInputs();
        InputsWithDirectory<InputFromManager> inputs =
                new InputsWithDirectory<>(inputList, Optional.empty());

        InputManagerParameters parameters = createMockParameters(true);

        InputsWithDirectory<InputFromManager> result =
                shuffler.inputsFromDelegate(inputs, parameters);

        assertEquals(inputList.size(), result.inputs().size());
        assertEquals(new HashSet<>(inputList), new HashSet<>(result.inputs()));
    }

    private List<InputFromManager> createInputs() {
        // A deliberately immutable list, to test if everything somehow still works out.
        return Streams.of(createMockInput("A"), createMockInput("B"), createMockInput("C"))
                .toList();
    }

    private InputFromManager createMockInput(String identifier) {
        InputFromManager mock = mock(InputFromManager.class);
        when(mock.identifier()).thenReturn(identifier);
        return mock;
    }

    private InputManagerParameters createMockParameters(boolean shuffle) {
        InputContextParameters contextParams = mock(InputContextParameters.class);
        when(contextParams.isShuffle()).thenReturn(shuffle);

        Logger logger = new Logger(mock(MessageLogger.class));

        InputManagerParameters params = mock(InputManagerParameters.class);
        when(params.getInputContext()).thenReturn(contextParams);
        when(params.getLogger()).thenReturn(logger);
        return params;
    }
}
