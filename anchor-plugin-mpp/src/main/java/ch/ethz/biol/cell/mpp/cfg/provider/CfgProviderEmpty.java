/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.provider;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.core.error.CreateException;

public class CfgProviderEmpty extends CfgProvider {

    @Override
    public Cfg create() throws CreateException {
        return new Cfg();
    }
}
