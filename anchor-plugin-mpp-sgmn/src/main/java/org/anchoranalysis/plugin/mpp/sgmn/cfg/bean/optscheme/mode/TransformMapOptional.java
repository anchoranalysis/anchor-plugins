/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;

public class TransformMapOptional<W, X> implements StateTransformer<Optional<W>, Optional<X>> {

    private StateTransformer<W, X> transformer;

    public TransformMapOptional(StateTransformer<W, X> transformer) {
        super();
        this.transformer = transformer;
    }

    @Override
    public Optional<X> transform(Optional<W> item, TransformationContext context)
            throws OperationFailedException {
        return OptionalUtilities.map(item, in -> transformer.transform(in, context));
    }
}
