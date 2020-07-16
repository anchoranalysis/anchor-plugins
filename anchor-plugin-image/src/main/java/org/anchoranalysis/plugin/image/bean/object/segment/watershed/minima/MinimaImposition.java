/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;

public abstract class MinimaImposition extends AnchorBean<MinimaImposition> {

    public abstract Channel imposeMinima(
            Channel chnl, SeedCollection seeds, Optional<ObjectMask> containingMask)
            throws OperationFailedException;
}
