/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.selectchnls;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderStackReference;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/**
 * Selects a specific set of channels from stacks, each identified by a stack-name and channel index
 *
 * @author Owen Feehan
 */
public class SelectSpecificAndRename extends SelectChnlsFromStacks {

    // START BEAN PROPERTIES
    @BeanField private List<NamedBean<ChnlProviderStackReference>> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public List<NamedChnl> selectChnls(ChannelSource source, boolean checkType)
            throws OperationFailedException {

        List<NamedChnl> out = new ArrayList<>();

        for (NamedBean<ChnlProviderStackReference> nb : list) {

            out.add(chnlFromRef(nb, source, checkType));
        }

        return out;
    }

    private static NamedChnl chnlFromRef(
            NamedBean<ChnlProviderStackReference> nb, ChannelSource source, boolean checkType)
            throws OperationFailedException {

        ChnlProviderStackReference ref = nb.getItem();

        Channel chnl = source.extractChnl(ref.getStackProviderID(), checkType, ref.getChnlIndex());

        return new NamedChnl(nb.getName(), chnl);
    }

    public List<NamedBean<ChnlProviderStackReference>> getList() {
        return list;
    }

    public void setList(List<NamedBean<ChnlProviderStackReference>> list) {
        this.list = list;
    }
}
