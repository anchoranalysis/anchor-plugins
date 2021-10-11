package org.anchoranalysis.plugin.io.bean.input;

import java.util.Collections;
import java.util.List;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helps shuffle elements in a list of inputs.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ShuffleHelper {

    /**
     * Randomizes the order of elements in a list of inputs, while also writing a log message.
     * 
     * <p>Note that {@code list} is manipulated in-place.
     * 
     * @param <T> element-type in list.
     * @param list the list to be shuffled.
     * @param params the {@link InputManager} parameters.
     * @return {@code list} with its elements shuffled.
     */
    public static <T> List<T> shuffleInputs(
            List<T> list, InputManagerParams params) {
        MessageLogger logger = params.getLogger().messageLogger();
        logger.log("Shuffling input order.");
        logger.logEmptyLine();
        Collections.shuffle(list);
        return list;
    }
}
