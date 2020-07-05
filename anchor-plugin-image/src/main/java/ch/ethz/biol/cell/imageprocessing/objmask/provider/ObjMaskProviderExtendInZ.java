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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// Extends an object as much as it can within the z-slices of a containing object
public class ObjMaskProviderExtendInZ extends ObjMaskProviderContainer {

	@Override
	public ObjectCollection createFromObjs(ObjectCollection objsSource) throws CreateException {
			
		List<MatchedObject> matchList = ObjMaskMatchUtilities.matchIntersectingObjects(
			containerRequired(),
			objsSource.duplicate()	// Duplicated so as to avoid cahanging the original
		);
		
		// For each obj we extend it into its container
		ObjectCollection out = new ObjectCollection();
		
		for( MatchedObject owm : matchList ) {
			for( ObjectMask omOther : owm.getMatches() ) {
				
				out.add(
					createExtendedObjMask(
						omOther,
						owm.getSourceObj()
					)
				);
			}
		}
		
		return out;
	}
		
	private static ObjectMask createExtendedObjMask(ObjectMask om, ObjectMask container) throws CreateException {
		
		ObjectMask omFlat = om.flattenZ();

		int zCent = (int) om.centerOfGravity().getZ();

		BoundingBox bbox = potentialZExpansion(omFlat, container);
		
		// We update these values after our intersection with the container, in case they have changed
		assert(container.getBoundingBox().contains().box(bbox));
		
		return ExtendObjsInZHelper.createExtendedObj(omFlat, container, bbox, zCent);
	}
	
	private static BoundingBox potentialZExpansion( ObjectMask omFlat, ObjectMask container ) throws CreateException {
		
		int zLow = container.getBoundingBox().cornerMin().getZ();
		int zHigh = container.getBoundingBox().calcCornerMax().getZ();
		
		Extent e = omFlat.getBoundingBox().extent().duplicateChangeZ(
			zHigh-zLow+1
		);
		ReadableTuple3i crnrMin = omFlat.getBoundingBox().cornerMin().duplicateChangeZ(zLow);
		
		return new BoundingBox( crnrMin, e ).intersection().with( container.getBoundingBox() ).orElseThrow( ()->
			new CreateException("Bounding boxes don't intersect")	
		);
	}
}
