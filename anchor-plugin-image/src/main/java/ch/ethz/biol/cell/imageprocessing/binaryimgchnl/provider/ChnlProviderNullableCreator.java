package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;

import org.anchoranalysis.bean.ProviderNullableCreator;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.ImageDim;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.DimChecker;

/**
 * Like {@link ProviderNullableCreator} but with specific methods for {@link Chnl} and {@link BinaryChnl} including checking channel sizes.
 * 
 * @author Owen Feehan
 *
 */
class ChnlProviderNullableCreator {

	private ChnlProviderNullableCreator() {}
	
	public static Optional<Chnl> createOptionalCheckSize(ChnlProvider chnlProvider, String chnlProviderName, ImageDim dim) throws CreateException {
		Optional<Chnl> chnl = ProviderNullableCreator.createOptional(chnlProvider);
		if (chnl.isPresent()) {
			DimChecker.check(chnl.get(), chnlProviderName, dim);
		}
		return chnl;
	}
	
	public static Optional<BinaryChnl> createOptionalCheckSize(BinaryChnlProvider chnlProvider, String chnlProviderName, ImageDim dim) throws CreateException {
		Optional<BinaryChnl> chnl = ProviderNullableCreator.createOptional(chnlProvider);
		if (chnl.isPresent()) {
			DimChecker.check(chnl.get(), chnlProviderName, dim);
		}
		return chnl;
	}
}
