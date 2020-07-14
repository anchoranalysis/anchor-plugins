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
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;

import lombok.Getter;
import lombok.Setter;

/**
 * Takes each object one-by-one from {@code objectsSource}, and finds matching seeds from {@code objectsSeeds}
 *  
 * @author Owen Feehan
 */
public class ObjMaskProviderSeededObjSgmn extends ObjMaskProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean @Getter @Setter
	private ObjectCollectionProvider objectsSource;
	
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objectsSeeds;
	
	@BeanField @Getter @Setter
	private SegmentChannelIntoObjects sgmn;
	// END BEAN PROPERTIES
	
	@Override
	protected ObjectCollection createFromChnl(Channel chnlSrc) throws CreateException {
		
		ObjectCollection seeds = objectsSeeds.create();
		
		if (objectsSource!=null) {
			ObjectCollection sourceObjs = objectsSource.create();
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
		SegmentChannelIntoObjects sgmn
	) throws CreateException {
		
		assert(seeds!=null);
		assert(sourceObjs!=null);
		
		List<MatchedObject> matchList = MatcherIntersectionHelper.matchIntersectingObjects(sourceObjs, seeds);

		return ObjectCollectionFactory.flatMapFrom(
			matchList.stream(),
			CreateException.class,
			ows -> sgmnIfMoreThanOne(ows, chnl, sgmn)
		);
	}
	
	private static ObjectCollection sgmnIfMoreThanOne(MatchedObject ows, Channel chnl, SegmentChannelIntoObjects sgmn) throws CreateException {
		if(ows.numMatches() <= 1) {
			return ObjectCollectionFactory.from( ows.getSource() );
		} else {
			try {
				return sgmn(ows, chnl, sgmn);
			} catch (SegmentationFailedException e) {
				throw new CreateException(e);
			}
		}
	}
	
	private static ObjectCollection createWithoutSourceObjs(
		Channel chnl,
		ObjectCollection seedsAsObjects,
		SegmentChannelIntoObjects sgmn
	) throws CreateException {
		
		try {
			return sgmn.segment(
				chnl,
				Optional.empty(),
				Optional.of(
					SeedsFactory.createSeedsWithoutMask(seedsAsObjects)
				)
			);
		} catch (SegmentationFailedException e) {
			throw new CreateException(e);
		}
	}
		
		
	// NB Objects in seeds are changed
	private static ObjectCollection sgmn(
		MatchedObject matchedObject,
		Channel channel,
		SegmentChannelIntoObjects sgmn
	) throws SegmentationFailedException, CreateException {
		
		BoundingBox bboxSource = matchedObject.getSource().getBoundingBox();
		
		// We create a new ObjMask for the new channel
		ObjectMask objectLocal = new ObjectMask(
			matchedObject.getSource().binaryVoxelBox()
		);
		
		SeedCollection seedsObj = SeedsFactory.createSeedsWithMask(
			matchedObject.getMatches(),
			objectLocal,
			bboxSource.cornerMin(),
			channel.getDimensions()
		);
		
		ObjectCollection sgmnObjs = sgmn.segment(
			createChannelForBox(channel, bboxSource),
			Optional.of(objectLocal),
			Optional.of(seedsObj)
		);
		
		// We shift each object back to were it belongs globally
		return sgmnObjs.shiftBy(
			bboxSource.cornerMin()
		);
	}
	
	private static Channel createChannelForBox(Channel channel, BoundingBox boundingBox) {
		// We make a channel just for the object
		return ChannelFactory.instance().create(
			channel.getVoxelBox().any().region(boundingBox, false),
			channel.getDimensions().getRes()
		);
	}
}
