/* (C)2020 */
package org.anchoranalysis.plugin.image.task.chnl.convert;

import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.chnl.ChnlGetter;

public class ChnlGetterForTimepoint {

    private ChnlGetter getter;
    private int t;

    public ChnlGetterForTimepoint(ChnlGetter getter, int t) {
        super();
        this.getter = getter;
        this.t = t;
    }

    public boolean hasChnl(String chnlName) {
        return getter.hasChnl(chnlName);
    }

    public Channel getChnl(String chnlName) throws GetOperationFailedException {
        return getter.getChnl(chnlName, t, ProgressReporterNull.get());
    }
}
