package org.anchoranalysis.plugin.image.task.bean.format;

import org.anchoranalysis.io.generator.sequence.OutputSequenceIndexed;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Owen Feehan
 *
 */
@Data @NoArgsConstructor
public class ChangeableOutputSequence<T,S> {

    private OutputSequenceIndexed<T, S> sequence;
}
