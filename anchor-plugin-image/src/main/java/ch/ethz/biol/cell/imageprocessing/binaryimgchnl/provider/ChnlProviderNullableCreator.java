/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.DimChecker;
import java.util.Optional;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Like {@link OptionalFactory} but with specific methods for {@link Channel} and {@link Mask}
 * including checking channel sizes.
 *
 * @author Owen Feehan
 */
class ChnlProviderNullableCreator {

    private ChnlProviderNullableCreator() {}

    public static Optional<Channel> createOptionalCheckSize(
            ChnlProvider chnlProvider, String chnlProviderName, ImageDimensions dim)
            throws CreateException {
        Optional<Channel> chnl = OptionalFactory.create(chnlProvider);
        if (chnl.isPresent()) {
            DimChecker.check(chnl.get(), chnlProviderName, dim);
        }
        return chnl;
    }

    public static Optional<Mask> createOptionalCheckSize(
            BinaryChnlProvider chnlProvider, String chnlProviderName, ImageDimensions dim)
            throws CreateException {
        Optional<Mask> chnl = OptionalFactory.create(chnlProvider);
        if (chnl.isPresent()) {
            DimChecker.check(chnl.get(), chnlProviderName, dim);
        }
        return chnl;
    }
}
