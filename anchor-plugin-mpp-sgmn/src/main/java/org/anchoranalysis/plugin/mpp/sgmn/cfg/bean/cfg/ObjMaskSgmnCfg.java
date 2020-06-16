package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;

/*
 * #%L
 * anchor-mpp-sgmn
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.name.provider.NamedProvider;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.experiment.bean.sgmn.SgmnObjMaskCollection;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.sgmn.bean.cfg.CfgSgmn;

public class ObjMaskSgmnCfg extends SgmnObjMaskCollection {

	// START BEAN PROPERTIES
	@BeanField
	private CfgSgmn cfgSgmn;
	// END BEAN PROPERTIES

	@Override
	public ObjectMaskCollection sgmn(
		NamedImgStackCollection stackCollection,
		NamedProvider<ObjectMaskCollection> objMaskCollection,
		Optional<SeedCollection> seeds,
		RandomNumberGenerator re,
		BoundIOContext context
	)
			throws SgmnFailedException {

		try {
			ImageDim sd = stackCollection.getException(ImgStackIdentifiers.INPUT_IMAGE).getDimensions();
			
			Cfg cfg = cfgSgmn.sgmn(
				stackCollection,
				objMaskCollection,
				Optional.empty(),
				context
			);
			return cfg.calcMask(
				sd,
				RegionMapSingleton.instance().membershipWithFlagsForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE),
				BinaryValuesByte.getDefault(),
				null
			).collectionObjMask();
			
		} catch (NamedProviderGetException e) {
			throw new SgmnFailedException(e.summarize());
		}
	}

	public CfgSgmn getCfgSgmn() {
		return cfgSgmn;
	}

	public void setCfgSgmn(CfgSgmn cfgSgmn) {
		this.cfgSgmn = cfgSgmn;
	}
}
