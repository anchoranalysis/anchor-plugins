package org.anchoranalysis.test.feature.plugins;

import static org.anchoranalysis.test.feature.plugins.ChnlFixture.MEDIUM;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;

public class NRGStackFixture {

	public static NRGStackWithParams create() throws CreateException {
	
		try {
			Stack stack = new Stack();
			stack.addChnl( ChnlFixture.createChnl(MEDIUM, ChnlFixture::sumMod) );
			stack.addChnl( ChnlFixture.createChnl(MEDIUM, ChnlFixture::diffMod) );
			stack.addChnl( ChnlFixture.createChnl(MEDIUM, ChnlFixture::multMod) );
			
			NRGStack nrgStack = new NRGStack(stack);
			return new NRGStackWithParams(nrgStack);
			
		} catch (IncorrectImageSizeException e) {
			assert false;
			return null;
		}
	}
}
