package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeParams;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;

public class NRGStackUtilities {

	public static FeatureCalculatorSingle<FeatureObjMaskParams> maybeAddNrgStack(
		FeatureCalculatorSingle<FeatureObjMaskParams> session,
		ChnlProvider chnlProvider
	) throws CreateException {
		
		if (chnlProvider!=null) {
			Chnl chnl = chnlProvider.create();
		
			// Make sure an NRG stack is added to each params that are called
			NRGStackWithParams nrgStack = new NRGStackWithParams(chnl); 
			return new FeatureCalculatorSingleChangeParams<>(
				session,
				params -> params.setNrgStack(nrgStack)
			);
		} else {
			return session;
		}
	}
}
