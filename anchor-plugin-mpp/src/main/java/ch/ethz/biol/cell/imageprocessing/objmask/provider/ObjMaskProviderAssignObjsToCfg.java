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
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjstocfg.RslvdEllipsoid;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjstocfg.RslvdEllipsoidList;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjstocfg.RslvdObjMask;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjstocfg.RslvdObjMaskList;

// Assigns objects into groups, based upon distance to objects in a configuration. One group per object in the configuration
public class ObjMaskProviderAssignObjsToCfg extends ObjMaskProviderDimensions {

	// START BEAN PROPERTIES
	@BeanField
	private CfgProvider cfgProvider;
	
	@BeanField
	private boolean mip = false;
	
	@BeanField
	private double maxDist = 0.0;		// A value <0 is OFF.  Imposes a maximum distance constraint on objs. Value in metres.
	
	@BeanField @OptionalBean
	private CfgProvider cfgProviderOutIncluded = null;		// OPTIONAL. Adds all included marks
	
	@BeanField @OptionalBean
	private CfgProvider cfgProviderOutExcluded = null;		// OPTIONAL. Adds all excluded marks
	
	@BeanField @OptionalBean
	private ObjMaskProvider objsOutUnassigned = null;	// Optional. All the objects not assigned anywhere at the end
	// END BEAN PROPERTIES
	
	private RegionMap regionMap = RegionMapSingleton.instance();
	private int regionID = 0;
	
	@Override
	public ObjectMaskCollection createFromObjs( ObjectMaskCollection objsCollection ) throws CreateException {
		
		Cfg cfg = cfgProvider.create();
		ImageDim dim = createDim();
		
		// EXIT EARLY when there are no ellipsoids
		if (cfg.size()==0) {
			// There are no ellipsoids to assign to, exit early making all objs unassigned
			if (objsOutUnassigned!=null) {
				ObjectMaskCollection objsOutUnassignedCollection = objsOutUnassigned.create();
				objsOutUnassignedCollection.addAll(objsCollection);
			}
			return new ObjectMaskCollection();
		}
		
		
		RslvdObjMaskList  listObjs = createRslvdObjMaskList( objsCollection );
		RslvdEllipsoidList listEllipsoids = createRslvdEllipsoidList(cfg, dim, regionMap.membershipWithFlagsForIndex(regionID), BinaryValuesByte.getDefault() );
		
		
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("START AssignObjsToCfg");
			getLogger().getLogReporter().logFormatted("Start\t%d objs for %d ellipsoids", listObjs.size(), listEllipsoids.size() );
		}
		
		// FIRST STEP, we assign all objects that are contained inside the ellipsoids, so long as they are exclusively in a single ellipsoid
		// If they are in more than one ellipsoid, we ignore the object, and exclude each ellipsoid
		listEllipsoids.assignContainedWithin(listObjs);
		if (mip) {
			listEllipsoids.assignContainedWithinMIP(listObjs);
		}
		
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().logFormatted("Contain\t%d objs unassigned, %d ellipsoids included, %d ellipsoids excluded", listObjs.size(), listEllipsoids.numIncluded(), listEllipsoids.numExcluded() );
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
			Cfg cfgOutExcluded = cfgProviderOutExcluded.create();
			listEllipsoids.addExcluded(cfgOutExcluded);
		}
		
		if (objsOutUnassigned!=null) {
			ObjectMaskCollection objsOutUnassignedCollection = objsOutUnassigned.create();
			listObjs.addAllTo(objsOutUnassignedCollection);
		}
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().logFormatted("Final\t%d objs unassigned, %d ellipsoids included, %d ellipsoids excluded", listObjs.size(), listEllipsoids.numIncluded(), listEllipsoids.numExcluded() );
			getLogger().getLogReporter().log("END AssignObjsToCfg");
		}
		
		// LAST STEP, for each included object, we merge all their associated object masks to form a new ObjMask
		return listEllipsoids.createMergedObjsForIncluded();
	}

	private static RslvdObjMaskList createRslvdObjMaskList( ObjectMaskCollection objs ) {
		RslvdObjMaskList out = new RslvdObjMaskList();
		for( ObjectMask om : objs ) {
			out.add( new RslvdObjMask(om) );
		}
		return out;
	}
		
	private static RslvdEllipsoidList createRslvdEllipsoidList( Cfg cfg, ImageDim dim, RegionMembershipWithFlags rm, BinaryValuesByte bvb ) throws CreateException {
		RslvdEllipsoidList out = new RslvdEllipsoidList();
		
		for( Mark m : cfg ) {
			
			if (m instanceof MarkEllipsoid) {
				out.add( new RslvdEllipsoid( (MarkEllipsoid) m, dim, rm, bvb ) );
			} else {
				throw new CreateException("Mark found that is not an ellipsoid");
			}
		}
		
		return out;
	}
	
	public CfgProvider getCfgProvider() {
		return cfgProvider;
	}

	public void setCfgProvider(CfgProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
	}

	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}

	public CfgProvider getCfgProviderOutIncluded() {
		return cfgProviderOutIncluded;
	}

	public void setCfgProviderOutIncluded(CfgProvider cfgProviderOutIncluded) {
		this.cfgProviderOutIncluded = cfgProviderOutIncluded;
	}

	public CfgProvider getCfgProviderOutExcluded() {
		return cfgProviderOutExcluded;
	}

	public void setCfgProviderOutExcluded(CfgProvider cfgProviderOutExcluded) {
		this.cfgProviderOutExcluded = cfgProviderOutExcluded;
	}

	public double getMaxDist() {
		return maxDist;
	}

	public void setMaxDist(double maxDist) {
		this.maxDist = maxDist;
	}

	public ObjMaskProvider getObjsOutUnassigned() {
		return objsOutUnassigned;
	}

	public void setObjsOutUnassigned(ObjMaskProvider objsOutUnassigned) {
		this.objsOutUnassigned = objsOutUnassigned;
	}
}
