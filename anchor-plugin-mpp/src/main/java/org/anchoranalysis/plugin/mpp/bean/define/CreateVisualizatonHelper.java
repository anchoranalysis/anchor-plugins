package org.anchoranalysis.plugin.mpp.bean.define;

import lombok.RequiredArgsConstructor;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.provider.ProviderAsStack;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredBase;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredMask;
import org.anchoranalysis.plugin.image.bean.stack.provider.color.ColoredObjects;
import org.anchoranalysis.plugin.image.provider.ReferenceFactory;
import org.anchoranalysis.plugin.mpp.bean.provider.collection.Reference;
import org.anchoranalysis.plugin.mpp.bean.stack.provider.ColoredMarks;

@RequiredArgsConstructor
class CreateVisualizatonHelper {

    private final String backgroundID;

    private final int outlineWidth;

    /** If TRUE, backgroundID refers to a Stack, otherwise it's a Channel */
    private final boolean stackBackground;

    public StackProvider mask(String maskProviderID) {
        ColoredMask provider = new ColoredMask();
        provider.setMask(ReferenceFactory.mask(maskProviderID));
        configureProvider(provider);
        return provider;
    }

    public StackProvider objects(String objectsProviderID) {
        ColoredObjects provider = new ColoredObjects();
        provider.setObjects(ReferenceFactory.objects(objectsProviderID));
        configureProvider(provider);
        return provider;
    }

    public StackProvider marks(String marksProviderID) {
        ColoredMarks provider = new ColoredMarks();
        provider.setMarks(new Reference(marksProviderID));
        configureProvider(provider);
        return provider;
    }

    private void configureProvider(ColoredBase provider) {
        provider.setOutlineWidth(outlineWidth);
        provider.setBackground(backgroundStack());
    }

    private ProviderAsStack backgroundStack() {
        if (stackBackground) {
            return ReferenceFactory.stack(backgroundID);
        } else {
            return ReferenceFactory.channel(backgroundID);
        }
    }
}
