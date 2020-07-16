/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Base class for {@link ObjectCollectionProvider} that take an optional {@code objectsContainer}
 * bean-field.
 *
 * @author Owen Feehan
 */
public abstract class ObjectCollectionProviderWithContainer extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsContainer;
    // END BEAN PROPERTIES

    protected Optional<ObjectCollection> containerOptional() throws CreateException {
        if (objectsContainer != null) {
            return Optional.of(objectsContainer.create());
        } else {
            return Optional.empty();
        }
    }

    protected ObjectCollection containerRequired() throws CreateException {
        return containerOptional()
                .orElseThrow(
                        () ->
                                new CreateException(
                                        "An objects-container must be defined for this provider"));
    }
}
