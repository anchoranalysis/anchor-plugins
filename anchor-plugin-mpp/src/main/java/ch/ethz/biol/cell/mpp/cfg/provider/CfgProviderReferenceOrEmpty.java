/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;

// Returns an empty set if it doesn't exist
public class CfgProviderReferenceOrEmpty extends CfgProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";
    // END BEAN PROPERTIES

    @Override
    public Cfg create() throws CreateException {

        try {
            Cfg cfg = getInitializationParameters().getCfgCollection().getException(id);

            if (cfg == null) {
                return new Cfg();
            }

            return cfg;
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
    }
}
