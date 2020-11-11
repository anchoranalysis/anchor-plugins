package org.anchoranalysis.plugin.mpp.experiment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.mpp.experiment.bean.ConvertNamedChannels;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Shared-state that remembers converted inputs for corresponding unconverted inputs.
 * 
 * <p>This class is intended as the shared-state for {@link ConvertNamedChannels}.
 * 
 * @author Owen Feehan
 *
 * @param <U> type an input is converted to
 * @param <S> shared-state type
 */
@RequiredArgsConstructor
public class SharedStateRememberConverted<U extends InputFromManager,S> {
    
    /** The shared state. */
    @Getter @Setter private S sharedState;
    
    /** 
     * A map of the unconverted inputs to the converted inputs.
     * 
     * <p>It is assumed that only one {@link JobProcessor} will ever be using this object simultaneously.
     */
    private Map<NamedChannelsInput,U> mapConverted = new HashMap<>();
    
    /**
     * Remembers a converted input.
     * 
     * @param unconverted the unconverted value
     * @param converted the converted value
     */
    public void rememberConverted(NamedChannelsInput unconverted, U converted) {
        mapConverted.put(unconverted, converted);
    }
    
    /**
     * Finds a converted-input that has been remembered.
     * 
     * @param unconverted the unconverted input
     * @return the corresponding convrted input for {@code unconverted}, if it exists.
     */
    public Optional<U> findConvertedInputFor(NamedChannelsInput unconverted) {
        return Optional.ofNullable(mapConverted.get(unconverted));
    }
    
    /**
     * Forgets all converted inputs.
     */
    public void forgetAll() {
        mapConverted.clear();
    }
}
