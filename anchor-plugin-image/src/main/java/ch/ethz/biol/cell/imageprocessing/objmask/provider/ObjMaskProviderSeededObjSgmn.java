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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SgmnFailedException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.MatchedObject;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// Takes each object one-by-one from objMaskProviderSourceObjs, and finds matching seeds from 
// objMaskProviderSeeds
//
// A segmentation is performed on the portion of chnlProviderSourceChnl
public class ObjMaskProviderSeededObjSgmn extends ObjMaskProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private ObjMaskProvider objsSource;
	
	@BeanField
	private ObjMaskProvider objsSeeds;
	
	@BeanField
	private ObjMaskSgmn sgmn;
	// END BEAN PROPERTIES
	
	@Override
	protected ObjectCollection createFromChnl(Channel chnlSrc) throws CreateException {
		
		ObjectCollection seeds = objsSeeds.create();
		
		if (objsSource!=null) {
			ObjectCollection sourceObjs = objsSource.create();
			return createWithSourceObjs(
				chnlSrc,
				seeds,
				sourceObjs,
				sgmn
			);
		} else {
			return createWithoutSourceObjs( chnlSrc, seeds, sgmn );
		}
	}
	
	private static ObjectCollection createWithSourceObjs(
		Channel chnl,
		ObjectCollection seeds,
		ObjectCollection sourceObjs,
		ObjMaskSgmn sgmn
	) throws CreateException {
		
		assert(seeds!=null);
		assert(sourceObjs!=null);
		
		List<MatchedObject> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( sourceObjs, seeds );
		
		ObjectCollection out = new ObjectCollection();
		for( MatchedObject ows : matchList ) {
			if( ows.numMatches() <= 1 ) {
				out.add( ows.getSourceObj() );
			} else {
				
				try {
					ObjectCollection objs = sgmn(
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
	
	private static ObjectCollection createWithoutSourceObjs(
		Channel chnl,
		ObjectCollection seedsAsObjs,
		ObjMaskSgmn sgmn
	) throws CreateException {
		
		SeedCollection seeds = SeedsFactory.createSeedsWithoutMask(seedsAsObjs);
		
		try {
			return sgmn.sgmn(
				chnl,
				Optional.empty(),
				Optional.of(seeds)
			);
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
	}
		
		
	// NB Objects in seeds are changed
	private static ObjectCollection sgmn(
		ObjectMask objMask,
		ObjectCollection seeds,
		Channel chnl,
		ObjMaskSgmn sgmn
	) throws SgmnFailedException, CreateException {
		
		// We make a channel just for the object
		VoxelBox<?> vb = chnl.getVoxelBox().any().region( objMask.getBoundingBox(), false);
		Channel chnlObjLocal = ChannelFactory.instance().create(vb, chnl.getDimensions().getRes());
		
		// We create a new ObjMask for the new channel
		ObjectMask objMaskLocal = new ObjectMask( objMask.binaryVoxelBox() );
		
		SeedCollection seedsObj = SeedsFactory.createSeedsWithMask(
			seeds,
			objMaskLocal,
			objMask.getBoundingBox().getCrnrMin(),
			chnl.getDimensions()
		);
		
		ObjectCollection sgmnObjs = sgmn.sgmn(
			chnlObjLocal,
			Optional.of(objMaskLocal),
			Optional.of(seedsObj)
		);
		
		// We shift each object back to were it belongs globally
		return sgmnObjs.shiftBy( objMask.getBoundingBox().getCrnrMin() );
	}
	
	

	public ObjMaskSgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(ObjMaskSgmn sgmn) {
		this.sgmn = sgmn;
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
