/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.selectchnls;

import java.util.List;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/** Selects a a subset of channels from a set of stacks */
public abstract class SelectChnlsFromStacks extends AnchorBean<SelectChnlsFromStacks> {

    /**
     * Takes a stack-collection and extracts a set of references to particular channels (each with a
     * name)
     */
    public abstract List<NamedChnl> selectChnls(ChannelSource source, boolean checkType)
            throws OperationFailedException;
}
