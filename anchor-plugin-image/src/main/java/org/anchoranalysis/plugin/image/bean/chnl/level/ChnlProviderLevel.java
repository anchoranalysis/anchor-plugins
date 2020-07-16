/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.chnl.level;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.DimChecker;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;

public abstract class ChnlProviderLevel extends ChnlProviderOne {

    // START BEAN PROPERTIES
    /** The input (an intensity channel in the normal way) */
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    @BeanField @Getter @Setter private ChnlProvider chnlOutput;

    @BeanField @Getter @Setter private CalculateLevel calculateLevel;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        return createFor(
                chnl, objects.create(), DimChecker.createSameSize(chnlOutput, "chnlOutput", chnl));
    }

    protected abstract Channel createFor(
            Channel chnlIntensity, ObjectCollection objects, Channel chnlOutput)
            throws CreateException;
}
