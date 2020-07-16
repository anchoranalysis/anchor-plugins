/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.plugin.image.bean.params.KeyValueCondition;

// If a param is equal to a particular value, do something
public class ChnlProviderIfParamEquals extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private KeyValueCondition condition;
    // END BEAN PROPERTIES

    @BeanField private ChnlProvider chnlElse;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        if (condition.isConditionTrue()) {
            return chnl;
        } else {
            return chnlElse.create();
        }
    }

    public ChnlProvider getChnlElse() {
        return chnlElse;
    }

    public void setChnlElse(ChnlProvider chnlElse) {
        this.chnlElse = chnlElse;
    }

    public KeyValueCondition getCondition() {
        return condition;
    }

    public void setCondition(KeyValueCondition condition) {
        this.condition = condition;
    }
}
