package org.anchoranalysis.plugin.mpp.experiment.feature.source;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;

@Value
@AllArgsConstructor
public class InitParamsWithNrgStack {

    ImageInitParams imageInit;
    NRGStackWithParams nrgStack;
}
