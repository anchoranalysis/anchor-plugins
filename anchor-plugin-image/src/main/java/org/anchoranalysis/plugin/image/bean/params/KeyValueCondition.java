/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.params;

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
    @BeanField private KeyValueParamsProvider params;

    @BeanField private String key = "";

    @BeanField private String value = "";
    // END BEAN PROPERTIES

    public boolean isConditionTrue() throws CreateException {
        KeyValueParams kvp = params.create();

        return value.equals(kvp.getProperty(key));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public KeyValueParamsProvider getParams() {
        return params;
    }

    public void setParams(KeyValueParamsProvider params) {
        this.params = params;
    }
}
