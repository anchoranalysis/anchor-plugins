/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.provider;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;

public class CfgProviderFromMark extends CfgProvider {

    /// START BEAN PROPERTIES
    @BeanField private MarkProvider markProvider;
    // END BEAN PROPERTIES

    @Override
    public Cfg create() throws CreateException {

        Cfg cfg = new Cfg();

        Optional<Mark> mark = markProvider.create();
        mark.ifPresent(cfg::add);
        return cfg;
    }

    public MarkProvider getMarkProvider() {
        return markProvider;
    }

    public void setMarkProvider(MarkProvider markProvider) {
        this.markProvider = markProvider;
    }
}
