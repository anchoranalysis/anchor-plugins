package org.anchoranalysis.plugin.image.bean.obj.merge;

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

import java.util.Optional;
import java.util.stream.Stream;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderContainer;
import lombok.Getter;
import lombok.Setter;

/** A base class for algorithms that merge obj-masks */
public abstract class ObjMaskProviderMergeBase extends ObjMaskProviderContainer {
	
	// START BEAN PROPERTIES
	/* Image-resolution */
	@BeanField @OptionalBean @Getter @Setter
	private ImageDimProvider dim;
	// END BEAN PROPERTIES
	
	@FunctionalInterface
	protected static interface MergeObjs {
		ObjectCollection mergeObjs( ObjectCollection objs ) throws OperationFailedException;
	}
		
	protected Optional<ImageResolution> calcResOptional() throws OperationFailedException {
		try {
			return OptionalFactory.create(dim).map(
				ImageDimensions::getRes
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	protected ImageResolution calcResRequired() throws OperationFailedException {
		return calcResOptional().orElseThrow( ()->
			new OperationFailedException("This algorithm requires an image-resolution to be set via resProvider")
		);
	}
	
	/**
	 * Merges either in a container, or altogether
	 * 
	 * @param objs
	 * @param mergeFunc a function that merges a collection of objects together (changes the collection in place)
	 * @return
	 * @throws OperationFailedException
	 */
	protected ObjectCollection mergeMultiplex( ObjectCollection objs, MergeObjs mergeFunc ) throws OperationFailedException {
		
		// To avoid changing the original
		ObjectCollection objsToMerge = objs.duplicateShallow();

		try {
			Optional<ObjectCollection> container = containerOptional();
			if (container.isPresent()) {
				return mergeInContainer(mergeFunc, objsToMerge, container.get());
			} else {
				return mergeAll(mergeFunc, objsToMerge);
			}
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static ObjectCollection mergeAll( MergeObjs merger, ObjectCollection objs) throws OperationFailedException {
		// TODO is this extra ObjectCollection constructor needed?
		return ObjectCollectionFactory.from(
			merger.mergeObjs(objs)	
		);
	}
	
	private static ObjectCollection mergeInContainer( MergeObjs merger, ObjectCollection objs, ObjectCollection containerObjs) throws OperationFailedException {
		
		// All matched objects
		Stream<ObjectCollection> matchesStream = ObjMaskMatchUtilities
				.matchIntersectingObjects(containerObjs, objs)
				.stream()
				.map(MatchedObject::getMatches);

		return ObjectCollectionFactory.flatMapFrom(
			matchesStream,
			OperationFailedException.class,
			merger::mergeObjs
		);		
	}
}
