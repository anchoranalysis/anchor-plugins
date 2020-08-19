package org.anchoranalysis.plugin.mpp.bean.define;

import org.anchoranalysis.image.bean.provider.BeanProviderAsStackBase;
import org.anchoranalysis.image.bean.provider.stack.Reference;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredBase;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredMask;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredObjects;
import org.anchoranalysis.plugin.mpp.bean.stack.provider.ColoredMarks;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderReference;
import ch.ethz.biol.cell.mpp.cfg.provider.CfgProviderReference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class CreateVisualizatonHelper {

    private final String backgroundID;

    private final int outlineWidth;

    /** If TRUE, backgroundID refers to a Stack, otherwise it's a Channel */
    private final boolean stackBackground;
    
    public StackProvider mask(String maskProviderID) {
        ColoredMask provider = new ColoredMask();
        provider.setMask(new org.anchoranalysis.plugin.image.bean.mask.provider.Reference(maskProviderID));
        configureProvider(provider);
        return provider;
    }

    public StackProvider objects(String objectsProviderID) {
        ColoredObjects provider = new ColoredObjects();
        provider.setObjects(new org.anchoranalysis.plugin.image.bean.object.provider.Reference(objectsProviderID));
        configureProvider(provider);
        return provider;
    }

    public StackProvider marks(String cfgProviderID) {
        ColoredMarks provider = new ColoredMarks();
        provider.setCfgProvider(new CfgProviderReference(cfgProviderID));
        configureProvider(provider);
        return provider;
    }
    
    private void configureProvider( ColoredBase provider ) {
        provider.setOutlineWidth(outlineWidth);
        provider.setBackground( backgroundStack() );        
    }

    private BeanProviderAsStackBase<?,?> backgroundStack() {
        if (stackBackground) {
            return new Reference(backgroundID);
        } else {
            return new ChnlProviderReference(backgroundID);
        }
    }
}
