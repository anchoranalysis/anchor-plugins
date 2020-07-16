/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.statereporter;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.TransformMapOptional;

public class StateReporterTransformer<T, S> extends StateReporter<Optional<T>, Optional<S>> {

    // START BEAN PROPERTIES
    @BeanField private StateTransformerBean<T, S> transformerPrimary;

    @BeanField @OptionalBean private StateTransformerBean<T, S> transformerSecondary;
    // END BEAN PROPERTIES

    @Override
    public StateTransformer<Optional<T>, Optional<S>> primaryReport() {
        return new TransformMapOptional<>(transformerPrimary);
    }

    @Override
    public Optional<StateTransformer<Optional<T>, Optional<S>>> secondaryReport() {
        return Optional.of(new TransformMapOptional<>(transformerSecondary));
    }

    public StateTransformerBean<T, S> getTransformerPrimary() {
        return transformerPrimary;
    }

    public void setTransformerPrimary(StateTransformerBean<T, S> transformerPrimary) {
        this.transformerPrimary = transformerPrimary;
    }

    public StateTransformerBean<T, S> getTransformerSecondary() {
        return transformerSecondary;
    }

    public void setTransformerSecondary(StateTransformerBean<T, S> transformerSecondary) {
        this.transformerSecondary = transformerSecondary;
    }
}
