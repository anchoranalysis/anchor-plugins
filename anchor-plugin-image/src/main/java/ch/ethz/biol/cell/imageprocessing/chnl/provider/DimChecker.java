package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.ImageDim;

public class DimChecker {
	
	public static void check(Chnl chnl, BinaryChnl mask) throws CreateException {
		if (!chnl.getDimensions().equals(mask.getDimensions())) {
			throw new CreateException( String.format("chnl (%s) and mask (%s) must have the same dimensions", chnl.getDimensions().toString(), mask.getDimensions().toString() ) );
		}
	}
	
	/**
	 * Checks a channel to make sure it's the same size as an an existing channel
	 * 
	 * @param chnlToCheck the channel whose size will be compared
	 * @param chnlToCheckName a user-meaningful string to identify the chnlToCheck in error messages
	 * @param dimFromChnl the dimensions it must equal from chnl (identified as chnl in error messages)
	 * @return the newly created channel
	 * @throws CreateException
	 */
	public static void check(Chnl chnlToCheck, String chnlToCheckName, ImageDim dimFromChnl) throws CreateException {
		check(
			chnlToCheck.getDimensions(),
			chnlToCheckName,
			dimFromChnl
		);
	}
	
	
	/**
	 * Checks a channel to make sure it's the same size as an an existing channel
	 * 
	 * @param chnlToCheck the channel whose size will be compared
	 * @param chnlToCheckName a user-meaningful string to identify the chnlToCheck in error messages
	 * @param dimFromChnl the dimensions it must equal from chnl (identified as chnl in error messages)
	 * @return the newly created channel
	 * @throws CreateException
	 */
	public static void check(BinaryChnl chnlToCheck, String chnlToCheckName, ImageDim dimFromChnl) throws CreateException {
		check(
			chnlToCheck.getDimensions(),
			chnlToCheckName,
			dimFromChnl
		);
	}
	
	/**
	 * Creates a new channel from a provider, making sure it's the same size as an an existing channel
	 * 
	 * @param provider the provider to to create the channel
	 * @param providerName a user-meaningful string to identify the provider in error messages
	 * @param chnlSameSize the channel which it must be the same size as (referred to in error messages as "chnl")
	 * @return the newly created channel
	 * @throws CreateException
	 */
	public static Chnl createSameSize(ChnlProvider provider, String providerName, Chnl chnlSameSize) throws CreateException {
		
		Chnl chnlNew = provider.create();
		check(chnlNew, providerName, chnlSameSize.getDimensions());
		return chnlNew;
	}
	
	
	/**
	 * Creates a new channel from a provider, making sure it's the same size as an an existing channel
	 * 
	 * @param provider the provider to to create the channel
	 * @param providerName a user-meaningful string to identify the provider in error messages
	 * @param chnlSameSize the channel which it must be the same size as (referred to in error messages as "chnl")
	 * @return the newly created channel
	 * @throws CreateException
	 */
	public static BinaryChnl createSameSize(BinaryChnlProvider provider, String providerName, Chnl chnlSameSize) throws CreateException {
		
		BinaryChnl chnlNew = provider.create();
		check(chnlNew, providerName, chnlSameSize.getDimensions());
		return chnlNew;
	}
	
	private static void check(ImageDim dimToCheck, String chnlToCheckName, ImageDim dimFromChnl) throws CreateException {
		if (!dimFromChnl.equals(dimToCheck)) {
			throw new CreateException(
				String.format(
					"chnl (%s) and %s (%s) must have the same dimensions",
					dimFromChnl.toString(),
					chnlToCheckName,
					dimToCheck.toString()
				)
			);
		}
	}
}