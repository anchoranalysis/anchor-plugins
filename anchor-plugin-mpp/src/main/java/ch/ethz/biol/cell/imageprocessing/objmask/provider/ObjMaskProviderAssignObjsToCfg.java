package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;

/*
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjectstocfg.ResolvedEllipsoid;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjectstocfg.ResolvedEllipsoidList;
import lombok.Getter;
import lombok.Setter;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjectstocfg.ResolvedObject;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjectstocfg.ResolvedObjectList;

// Assigns objects into groups, based upon distance to objects in a configuration. One group per object in the configuration
public class ObjMaskProviderAssignObjsToCfg extends ObjMaskProviderDimensions {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private CfgProvider cfgProvider;
	
	@BeanField @Getter @Setter
	private boolean mip = false;
	
	/** 
	 * Imposes a maximum distance constraint on objects (if positive).
	 * <p>
	 * The value is expected in metres.
	 * <p>
	 * A value of {@code <=0) disables the constraint.
	 **/
	@BeanField @Getter @Setter
	private double maxDist = 0.0; 
	
	/** The marks that are included are added to this provider */
	@BeanField @OptionalBean @Getter @Setter
	private CfgProvider cfgProviderOutIncluded;
	
	/** The marks that are excluded are added to this provider */
	@BeanField @OptionalBean @Getter @Setter
	private CfgProvider cfgProviderOutExcluded;
	
	/** The objects that remain unassigned upon completion of the algorithm are added to this provider. */
	@BeanField @OptionalBean @Getter @Setter
	private ObjectCollectionProvider objectsOutUnassigned; 
	// END BEAN PROPERTIES
	
	private RegionMap regionMap = RegionMapSingleton.instance();
	private int regionID = 0;
	
	@Override
	public ObjectCollection createFromObjects( ObjectCollection objectCollection ) throws CreateException {
		
		Cfg cfg = cfgProvider.create();
		ImageDimensions dim = createDim();
		
		// EXIT EARLY when there are no ellipsoids
		if (cfg.size()==0) {
			// There are no ellipsoids to assign to, exit early making all objects unassigned
			if (objectsOutUnassigned!=null) {
				objectsOutUnassigned.create().addAll(objectCollection);
			}
			return ObjectCollectionFactory.empty();
		}
				
		ResolvedObjectList listObjs = createResolvedObjectList( objectCollection );
		ResolvedEllipsoidList listEllipsoids = createRslvdEllipsoidList(cfg, dim, regionMap.membershipWithFlagsForIndex(regionID), BinaryValuesByte.getDefault() );
		
		
		
		if (getLogger()!=null) {
			getLogger().messageLogger().log("START AssignObjsToCfg");
			getLogger().messageLogger().logFormatted("Start\t%d objects for %d ellipsoids", listObjs.size(), listEllipsoids.size() );
		}
		
		// FIRST STEP, we assign all objects that are contained inside the ellipsoids, so long as they are exclusively in a single ellipsoid
		// If they are in more than one ellipsoid, we ignore the object, and exclude each ellipsoid
		listEllipsoids.assignContainedWithin(listObjs);
		if (mip) {
			listEllipsoids.assignContainedWithinMIP(listObjs);
		}
		
		
		if (getLogger()!=null) {
			getLogger().messageLogger().logFormatted("Contain\t%d objects unassigned, %d ellipsoids included, %d ellipsoids excluded", listObjs.size(), listEllipsoids.numIncluded(), listEllipsoids.numExcluded() );
		}
		
		listEllipsoids.assignClosestEllipsoids( listObjs, maxDist );
		
		// We mark any ellipsoids that touch the border as xcluded
		listEllipsoids.excludeBorderXYObjects();
		listEllipsoids.excludeEmptyEllipsoids();
		
		// Included
		if( cfgProviderOutIncluded!=null ) {
			Cfg cfgOutIncluded = cfgProviderOutIncluded.create();
			listEllipsoids.addIncluded(cfgOutIncluded);
		}
		
		// Excluded
		if( cfgProviderOutExcluded!=null ) {
			listEllipsoids.addExcluded(
				cfgProviderOutExcluded.create()
			);
		}
		
		if (objectsOutUnassigned!=null) {
			listObjs.addAllTo(
				objectsOutUnassigned.create()
			);
		}
		
		if (getLogger()!=null) {
			getLogger().messageLogger().logFormatted("Final\t%d objects unassigned, %d ellipsoids included, %d ellipsoids excluded", listObjs.size(), listEllipsoids.numIncluded(), listEllipsoids.numExcluded() );
			getLogger().messageLogger().log("END AssignObjsToCfg");
		}
		
		// LAST STEP, for each included object, we merge all their associated object masks to form a new ObjMask
		return listEllipsoids.createMergedObjsForIncluded();
	}

	private static ResolvedObjectList createResolvedObjectList( ObjectCollection objects ) {
		return new ResolvedObjectList(
			objects.streamStandardJava().map(ResolvedObject::new)
		);
	}
		
	private static ResolvedEllipsoidList createRslvdEllipsoidList( Cfg cfg, ImageDimensions dim, RegionMembershipWithFlags rm, BinaryValuesByte bvb ) throws CreateException {
		ResolvedEllipsoidList out = new ResolvedEllipsoidList();
		
		for( Mark m : cfg ) {
			
			if (m instanceof MarkEllipsoid) {
				out.add( new ResolvedEllipsoid( (MarkEllipsoid) m, dim, rm, bvb ) );
			} else {
				throw new CreateException("Mark found that is not an ellipsoid");
			}
		}
		
		return out;
	}
}
