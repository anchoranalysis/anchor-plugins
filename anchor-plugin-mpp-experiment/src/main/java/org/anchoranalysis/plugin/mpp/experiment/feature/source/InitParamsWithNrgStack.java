package org.anchoranalysis.plugin.mpp.experiment.feature.source;

import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class InitParamsWithNrgStack {
    
    ImageInitParams imageInit;
    NRGStackWithParams nrgStack;
}
