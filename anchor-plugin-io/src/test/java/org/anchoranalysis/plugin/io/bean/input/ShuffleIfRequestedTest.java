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
