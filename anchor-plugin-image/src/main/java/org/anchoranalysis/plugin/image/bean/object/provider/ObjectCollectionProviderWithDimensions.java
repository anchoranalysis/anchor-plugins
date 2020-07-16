/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Base class for {@link ObjectCollectionProviderUnary} classes that require dimensions to be
 * specified.
 *
 * @see ObjectCollectionProviderWithOptionalDimensions for a similar class with optional dimension
 *     specification.
 * @author Owen Feehan
 */
public abstract class ObjectCollectionProviderWithDimensions extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ImageDimProvider dim = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    protected ImageDimensions createDimensions() throws CreateException {
        return dim.create();
    }
}
