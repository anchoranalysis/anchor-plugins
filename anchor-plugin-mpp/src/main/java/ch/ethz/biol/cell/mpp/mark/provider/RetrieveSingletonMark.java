/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.provider;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;

// Retrieves a mark from a cfg, assuming there is only one mark in a cfg, otherwise throwing an
// error
public class RetrieveSingletonMark extends MarkProvider {

    // START BEAN PROPERTIES
    @BeanField private CfgProvider cfgProvider;
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> create() throws CreateException {
        Cfg cfg = cfgProvider.create();

        if (cfg.size() == 0) {
            throw new CreateException("Cfg is empty. It must have exactly one item");
        }

        if (cfg.size() > 1) {
            throw new CreateException("Cfg has multiple marks. It must have exactly one item");
        }

        return Optional.of(cfg.get(0));
    }

    public CfgProvider getCfgProvider() {
        return cfgProvider;
    }

    public void setCfgProvider(CfgProvider cfgProvider) {
        this.cfgProvider = cfgProvider;
    }
}
