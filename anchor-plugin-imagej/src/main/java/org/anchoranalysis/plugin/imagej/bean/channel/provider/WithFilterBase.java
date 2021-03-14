package org.anchoranalysis.plugin.imagej.bean.channel.provider;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.plugin.imagej.channel.provider.FilterHelper;

@AllArgsConstructor
public abstract class WithFilterBase extends WithRadiusBase {

    /** The filter to apply, as represented by an ImageJ constant. */
    private int rankFilter;

    @Override
    public Channel createFromChannel(Channel channel, int radius) throws CreateException {
        try {
            return FilterHelper.applyRankFilter(channel, radius, rankFilter);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
