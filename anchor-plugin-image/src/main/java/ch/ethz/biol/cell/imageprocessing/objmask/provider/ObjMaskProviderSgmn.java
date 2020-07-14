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


import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;
import org.anchoranalysis.image.seed.SeedCollection;

import lombok.Getter;
import lombok.Setter;

public class ObjMaskProviderSgmn extends ObjMaskProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean @Getter @Setter
	private BinaryChnlProvider mask;
	
	@BeanField @Getter @Setter
	private SegmentChannelIntoObjects sgmn;
	
	@BeanField @OptionalBean @Getter @Setter
	private ObjectCollectionProvider objectsSeeds;
	// END BEAN PROPERTIES

	@Override
	protected ObjectCollection createFromChnl(Channel chnlSource) throws CreateException {

		Optional<ObjectMask> maskAsObject = createMask();
	
		try {
			return sgmn.segment(
				chnlSource,
				maskAsObject,
				createSeeds(
					chnlSource.getDimensions(),
					maskAsObject
				)
			);
		} catch (SegmentationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	private Optional<ObjectMask> createMask() throws CreateException {
		return OptionalFactory.create(mask).map(
			CreateFromEntireChnlFactory::createObject
		);
	}
	
	private Optional<SeedCollection> createSeeds(
		ImageDimensions dim,
		Optional<ObjectMask> maskAsObject
	) throws CreateException {
		return OptionalUtilities.map(
			OptionalFactory.create(objectsSeeds),
			objects-> createSeeds(
				objects,
				maskAsObject,
				dim
			) 
		);
	}
	
	private static SeedCollection createSeeds(
		ObjectCollection seeds,
		Optional<ObjectMask> maskAsObject,
		ImageDimensions dim
	) throws CreateException {
		return OptionalUtilities.map(
			maskAsObject,
			mask -> SeedsFactory.createSeedsWithMask(
				seeds,
				mask,
				new Point3i(0,0,0),
				dim
			)
		).orElseGet( ()->
			SeedsFactory.createSeedsWithoutMask(seeds)
		);
	}
}
