package org.anchoranalysis.plugin.image.bean.object.provider.segment;

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
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithChannel;

import lombok.Getter;
import lombok.Setter;

/**
 * Takes each object one-by-one from {@code objectsSource}, and finds matching seeds from {@code objectsSeeds}
 *  
 * @author Owen Feehan
 */
public class SegmentWithSeeds extends ObjectCollectionProviderWithChannel {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean @Getter @Setter
	private ObjectCollectionProvider objectsSource;
	
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objectsSeeds;
	
	@BeanField @Getter @Setter
	private SegmentChannelIntoObjects sgmn;
	// END BEAN PROPERTIES
	
	@Override
	protected ObjectCollection createFromChnl(Channel channel) throws CreateException {
		
		ObjectCollection seeds = objectsSeeds.create();
		
		if (objectsSource!=null) {
			ObjectCollection sourceObjects = objectsSource.create();
			return createWithSourceObjects(
				channel,
				seeds,
				sourceObjects,
				sgmn
			);
		} else {
			return createWithoutSourceObjects(channel, seeds, sgmn);
		}
	}
	
	private static ObjectCollection createWithSourceObjects(
		Channel chnl,
		ObjectCollection seeds,
		ObjectCollection sourceObjects,
		SegmentChannelIntoObjects segment
	) throws CreateException {
		
		assert(seeds!=null);
		assert(sourceObjects!=null);
		
		List<MatchedObject> matchList = MatcherIntersectionHelper.matchIntersectingObjects(sourceObjects, seeds);

		return ObjectCollectionFactory.flatMapFrom(
			matchList.stream(),
			CreateException.class,
			ows -> segmentIfMoreThanOne(ows, chnl, segment)
		);
	}
	
	private static ObjectCollection segmentIfMoreThanOne(
		MatchedObject ows,
		Channel channel,
		SegmentChannelIntoObjects segment
	) throws CreateException {
		if(ows.numMatches() <= 1) {
			return ObjectCollectionFactory.from( ows.getSource() );
		} else {
			try {
				return sgmn(ows, channel, segment);
			} catch (SegmentationFailedException e) {
				throw new CreateException(e);
			}
		}
	}
	
	private static ObjectCollection createWithoutSourceObjects(
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
		
		// We create a new object-mask for the new channel
		ObjectMask objectLocal = new ObjectMask(
			matchedObject.getSource().binaryVoxelBox()
		);
		
		SeedCollection seedsObj = SeedsFactory.createSeedsWithMask(
			matchedObject.getMatches(),
			objectLocal,
			bboxSource.cornerMin(),
			channel.getDimensions()
		);
		
		ObjectCollection sgmnObjects = sgmn.segment(
			createChannelForBox(channel, bboxSource),
			Optional.of(objectLocal),
			Optional.of(seedsObj)
		);
		
		// We shift each object back to were it belongs globally
		return sgmnObjects.shiftBy(
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