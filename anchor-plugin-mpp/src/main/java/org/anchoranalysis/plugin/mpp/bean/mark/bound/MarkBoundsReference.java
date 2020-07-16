/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.bound;

import org.anchoranalysis.anchor.mpp.bean.bound.MarkBounds;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkBoundsProvider;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;

public class MarkBoundsReference extends MarkBoundsProvider {

    // Start BEAN
    @BeanField private String id;
    // End BEAN

    @Override
    public MarkBounds create() throws CreateException {
        try {
            return getInitializationParameters().getMarkBoundsSet().getException(id);
        } catch (NamedProviderGetException e) {
            throw new CreateException(e.summarize());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
