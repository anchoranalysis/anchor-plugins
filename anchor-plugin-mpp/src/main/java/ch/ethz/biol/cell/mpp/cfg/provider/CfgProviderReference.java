/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.provider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;

@NoArgsConstructor
public class CfgProviderReference extends CfgProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String id = "";
    // END BEAN PROPERTIES

    public CfgProviderReference(String id) {
        super();
        this.id = id;
    }

    @Override
    public Cfg create() throws CreateException {
        try {
            return getInitializationParameters().getCfgCollection().getException(id);
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
    }
}
