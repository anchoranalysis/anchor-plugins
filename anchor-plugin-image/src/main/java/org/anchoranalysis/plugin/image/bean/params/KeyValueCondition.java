/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.params;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.ImageBean;

/**
 * A key and associated value
 *
 * @author Owen Feehan
 */
public class KeyValueCondition extends ImageBean<KeyValueCondition> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KeyValueParamsProvider params;

    @BeanField @Getter @Setter private String key = "";

    @BeanField @Getter @Setter private String value = "";
    // END BEAN PROPERTIES

    public boolean isConditionTrue() throws CreateException {
        KeyValueParams kvp = params.create();

        return value.equals(kvp.getProperty(key));
    }
}
