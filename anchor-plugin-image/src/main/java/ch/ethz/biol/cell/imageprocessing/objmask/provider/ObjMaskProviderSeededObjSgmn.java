package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*
 * #%L
 * anchor-plugin-image
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


import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// Takes each object one-by-one from objMaskProviderSourceObjs, and finds matching seeds from 
// objMaskProviderSeeds
//
// A segmentation is performed on the portion of chnlProviderSourceChnl
public class ObjMaskProviderSeededObjSgmn extends ObjMaskProvider {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private ObjMaskProvider objsSource;
	
	@BeanField
	private ObjMaskProvider objsSeeds;
	
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private ObjMaskSgmn sgmn;
	// END BEAN PROPERTIES
	
	// NB Objects in seeds are changed
	private static ObjMaskCollection sgmn(
		ObjMask objMask,
		ObjMaskCollection seeds,
		Chnl chnl,
		ObjMaskSgmn sgmn
	) throws SgmnFailedException, CreateException {
		
		// We make a channel just for the object
		VoxelBox<?> vb = chnl.getVoxelBox().any().createBufferAlwaysNew( objMask.getBoundingBox() );
		Chnl chnlObjLocal = ChnlFactory.instance().create(vb, chnl.getDimensions().getRes());
		
		// We create a new ObjMask for the new channel
		ObjMask objMaskLocal = new ObjMask( objMask.binaryVoxelBox() );
		
		SeedCollection seedsObj = SeedsFactory.createSeedsWithMask(
			seeds,
			objMaskLocal,
			objMask.getBoundingBox().getCrnrMin(),
			chnl.getDimensions()
		);
		
		ObjMaskCollection sgmnObjs = sgmn.sgmn(
			chnlObjLocal,
			objMaskLocal,
			seedsObj
		);
		
		// We shift each object back to were it belongs globally
		for( ObjMask om : sgmnObjs ) {
			om.getBoundingBox().getCrnrMin().add( objMask.getBoundingBox().getCrnrMin() );
		}
		
		return sgmnObjs;
	}
	
	
	public static ObjMaskCollection createWithSourceObjs(
		Chnl chnl,
		ObjMaskCollection seeds,
		ObjMaskCollection sourceObjs,
		ObjMaskSgmn sgmn
	) throws CreateException {
		
		assert(seeds!=null);
		assert(sourceObjs!=null);
		
		List<ObjWithMatches> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( sourceObjs, seeds );
		
		ObjMaskCollection out = new ObjMaskCollection();
		for( ObjWithMatches ows : matchList ) {
			if( ows.numMatches() <= 1 ) {
				out.add( ows.getSourceObj() );
			} else {
				
				try {
					ObjMaskCollection objs = sgmn(
						ows.getSourceObj(),
						ows.getMatches(),
						chnl,
						sgmn
					);
					
					out.addAll( objs );
				} catch (SgmnFailedException e) {
					throw new CreateException(e);
				}
			}
		}
		return out;
	}
	
	private static ObjMaskCollection createWithoutSourceObjs(
		Chnl chnl,
		ObjMaskCollection seedsAsObjs,
		ObjMaskSgmn sgmn
	) throws CreateException {
		
		SeedCollection seeds = SeedsFactory.createSeedsWithoutMask(seedsAsObjs);
		
		try {
			return sgmn.sgmn(chnl, seeds);
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
	}
	
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		
		ObjMaskCollection seeds = objsSeeds.create();
		
		if (objsSource!=null) {
			ObjMaskCollection sourceObjs = objsSource.create();
			return createWithSourceObjs(
				chnl,
				seeds,
				sourceObjs,
				sgmn
			);
		} else {
			return createWithoutSourceObjs( chnl, seeds, sgmn );
		}
	}

	public ObjMaskSgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(ObjMaskSgmn sgmn) {
		this.sgmn = sgmn;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public ObjMaskProvider getObjsSeeds() {
		return objsSeeds;
	}


	public void setObjsSeeds(ObjMaskProvider objsSeeds) {
		this.objsSeeds = objsSeeds;
	}


	public ObjMaskProvider getObjsSource() {
		return objsSource;
	}


	public void setObjsSource(ObjMaskProvider objsSource) {
		this.objsSource = objsSource;
	}
}
