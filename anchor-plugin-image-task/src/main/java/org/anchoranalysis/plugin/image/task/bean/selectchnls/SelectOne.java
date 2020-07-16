/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.selectchnls;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/**
 * Selects one specific channel from a set of stacks
 *
 * @author Owen Feehan
 */
public class SelectOne extends SelectChnlsFromStacks {

    // START BEAN FIELDS
    /**
     * If defined, processing only occurs the stack with this specific stack and index. Otherwise
     * processing occurs on all input stacks
     */
    @BeanField private String stackName;

    @BeanField private int chnlIndex = 0;
    // END BEAN FIELDS

    @Override
    public List<NamedChnl> selectChnls(ChannelSource source, boolean checkType)
            throws OperationFailedException {
        Channel chnl = source.extractChnl(stackName, checkType, chnlIndex);

        List<NamedChnl> out = new ArrayList<>();
        out.add(new NamedChnl(stackName, chnl));
        return out;
    }
}
