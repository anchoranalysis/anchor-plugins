/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.mpp.experiment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.mpp.experiment.bean.ConvertNamedChannels;

/**
 * Shared-state that remembers converted inputs for corresponding unconverted inputs.
 *
 * <p>This class is intended as the shared-state for {@link ConvertNamedChannels}.
 *
 * @author Owen Feehan
 * @param <U> type an input is converted to
 * @param <S> shared-state type
 */
@RequiredArgsConstructor
public class SharedStateRememberConverted<U extends InputFromManager, S> {

    /** The shared state. */
    @Getter @Setter private S sharedState;

    /**
     * A map of the unconverted inputs to the converted inputs.
     *
     * <p>It is assumed that only one {@link JobProcessor} will ever be using this object
     * simultaneously.
     */
    private Map<NamedChannelsInput, U> mapConverted = new HashMap<>();

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

    /** Forgets all converted inputs. */
    public void forgetAll() {
        mapConverted.clear();
    }
}
