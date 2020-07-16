/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Base class for {@link ObjectCollectionProviderUnary} classes that offer an optional means to
 * specify dimensions.
 *
 * @see ObjectCollectionProviderWithDimensions for a similar class with mandatory dimension
 *     specification.
 * @author Owen Feehan
 */
public abstract class ObjectCollectionProviderWithOptionalDimensions
        extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ImageDimProvider dim;
    // END BEAN PROPERTIES

    /** Returns the dimensions or NULL if none are provided */
    protected Optional<ImageDimensions> createDims() throws CreateException {
        if (dim != null) {
            return Optional.of(dim.create());
        } else {
            return Optional.empty();
        }
    }
}
