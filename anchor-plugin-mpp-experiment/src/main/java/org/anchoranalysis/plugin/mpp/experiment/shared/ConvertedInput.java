package org.anchoranalysis.plugin.mpp.experiment.shared;

import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.io.input.InputFromManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 * A converted-input, together with any logged messages associated with it.
 * @author Owen Feehan
 *
 * @param <U> type an input is converted to
 */
@AllArgsConstructor @Value
public class ConvertedInput<U extends InputFromManager> {
    
    /** The converted input. */
    @Getter private U conversion;
    
    /** Any messages written to the log during conversion. */
    private StringBuilder messages;
    
    /**
     * Logs any messages stored from from conversion.
     *  
     * @param logger the logger to log to.
     */
    public void logConversionMessages(MessageLogger logger) {
        String loggedMessages = messages.toString();
        if (!loggedMessages.isEmpty()) {
            logger.log(loggedMessages);
        }
    }
}