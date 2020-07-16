/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.statereporter;

import java.util.Optional;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;

/**
 * Assumes the state is reported without any transformation
 *
 * @author Owen Feehan
 * @param <T>
 */
public class StateReporterIdentity<T> extends StateReporter<T, T> {

    @Override
    public StateTransformer<T, T> primaryReport() {
        return (a, context) -> a;
    }

    @Override
    public Optional<StateTransformer<T, T>> secondaryReport() {
        return Optional.empty();
    }
}
