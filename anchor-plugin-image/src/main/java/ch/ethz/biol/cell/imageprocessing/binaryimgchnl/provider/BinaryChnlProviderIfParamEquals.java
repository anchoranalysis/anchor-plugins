/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.plugin.image.bean.params.KeyValueCondition;

// If a param is equal to a particular value, do soemthing
public class BinaryChnlProviderIfParamEquals extends BinaryChnlProviderElseBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KeyValueCondition condition;
    // END BEAN PROPERTIES

    @Override
    protected boolean condition(Mask chnl) throws CreateException {
        return condition.isConditionTrue();
    }
}
