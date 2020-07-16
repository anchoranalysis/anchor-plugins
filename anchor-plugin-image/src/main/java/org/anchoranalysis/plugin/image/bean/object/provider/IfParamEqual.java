/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Multiplexes between two object-collection-providers depending on whether a parameter value equals
 * a particular string
 *
 * <p>If the parameter value doesn't exist or is null, an exception is thrown.
 *
 * @author Owen Feehan
 */
public class IfParamEqual extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider whenEqual;

    @BeanField @Getter @Setter private ObjectCollectionProvider whenNotEqual;

    @BeanField @Getter @Setter private KeyValueParamsProvider keyValueParamsProvider;

    @BeanField @Getter @Setter private String key;

    @BeanField @Getter @Setter private String value;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        String valFromProp = keyValueParamsProvider.create().getProperty(key);

        if (valFromProp == null) {
            throw new CreateException(String.format("property-value for (%s) is null", key));
        }

        if (valFromProp.equals(value)) {
            return whenEqual.create();
        } else {
            return whenNotEqual.create();
        }
    }
}
