package org.anchoranalysis.plugin.mpp.bean.proposer.points;

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


import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundUnitless;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposeVisualizationList;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistanceVoxels;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.points.PointsFromBinaryChnl;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.PointsFromOrientationProposer;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler.VisitSchedulerConvexAboutRoot;


public class XYOrientationExtendToZ extends PointsProposer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private OrientationProposer orientationXYProposer;		// Should find an orientation in the XY plane
	
	@BeanField
	private PointsFromOrientationProposer pointsFromOrientationXYProposer;
	
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField @OptionalBean
	private BinaryImgChnlProvider binaryImgChnlProviderFilled;
	
	@BeanField
	private ScalarProposer maxDistanceZ;
	
	@BeanField
	private int minNumSlices = 3;
	
	@BeanField
	private int forceMinZSize = 1;
	
	@BeanField
	private boolean forwardDirectionOnly = false;
	
	// If we reach this amount of slices without adding a point, we consider our job over
	@BeanField
	private UnitValueDistance distanceZEndIfEmpty = new UnitValueDistanceVoxels(1000000);
	// END BEAN PROPERTIES

	private List<Point3i> lastPntsAll;
	
	public static class PointListForConvex {
		private List<Point3i> list = new ArrayList<Point3i>();

		public boolean add(Point3i e) {
			return list.add(e);
		}
		
		public boolean convexWithAtLeastOnePoint( Point3i root, Point3i pnt, BinaryVoxelBox<ByteBuffer> vb  ) {
			return VisitSchedulerConvexAboutRoot.isPointConvexTo(root, pnt, vb);
		}
		
		public boolean convexWithAtLeastOnePoint( Point3i pnt, BinaryVoxelBox<ByteBuffer> vb  ) {
			
			for( Point3i p : list ) {
				if (convexWithAtLeastOnePoint(p, pnt, vb)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	@Override
	public List<Point3i> propose(Point3d pnt, Mark mark, ImageDim sceneDim, RandomNumberGenerator re, ErrorNode errorNode) {
		
		pointsFromOrientationXYProposer.clearVisualizationState();
		
		
		
		
		Orientation orientation;
		
		//Point3d pntRoot = new Point3d(pnt);

		orientation = orientationXYProposer.propose(mark, sceneDim, re, errorNode.add("orientationProposer") );
		
		if (orientation==null) {
			return null;
		}

		try {
			int maxZDist = (int) Math.round( maxDistanceZ.propose(re, sceneDim.getRes()) );
			
			maxZDist = Math.max(maxZDist, minNumSlices);
			
			int skipZDist = (int) Math.round(distanceZEndIfEmpty.rslv(sceneDim.getRes(), new DirectionVector(0, 0, 1.0)) );
			skipZDist = 100;
			
			BinaryChnl chnl = binaryImgChnlProvider.create();
			BinaryChnl chnlFilled = binaryImgChnlProviderFilled!=null ? binaryImgChnlProviderFilled.create() : null;
			
			
			List<List<Point3i>> pnts = pointsFromOrientationXYProposer.calcPoints( pnt, orientation, sceneDim.getZ()>1, re, forwardDirectionOnly );

			// We take the first point in each list, as where it intersects with the edge
			
			PointListForConvex pl = new PointListForConvex();
			for( List<Point3i> list : pnts ) {
				if (list.size()>0) {
					pl.add( list.get(0) );
				}
			}
		// We get all the points which are high from the binary channel
		
			
			lastPntsAll = new ArrayList<Point3i>();
			//lastPntsAll.clear();
			
			for( List<Point3i> contourPnts : pnts ) {
				lastPntsAll.addAll( extendedPoints( contourPnts, pnt, pl, maxZDist, skipZDist, chnl, chnlFilled, sceneDim ) );
			}
			
			
			BoundUnitless bound = zSize(lastPntsAll);
			if (bound.size()<forceMinZSize) {
				lastPntsAll = forceMinimumZSize(lastPntsAll,bound);	
			}
			
			//System.out.printf("Points inside box %d\n", lastPntsAll.size() );
			
			return lastPntsAll;
			
		} catch (CreateException | OperationFailedException | TraverseOutlineException e1) {
			errorNode.add(e1);
			return null;
		}
	}
	
	
	
	public static List<Point3i> extendedPoints( List<Point3i> pntsAlongContour, Point3d pntRoot, PointListForConvex pl, int maxZDist, int skipZDist, BinaryChnl chnl, BinaryChnl chnlFilled, ImageDim sceneDim ) throws OperationFailedException, CreateException {
		
		BoundingBox bbox = BoundingBoxFromPoints.forList(pntsAlongContour);

		int zLow = Math.max(0, bbox.getCrnrMin().getZ()-maxZDist );
		int zHigh = Math.min(sceneDim.getZ(), bbox.getCrnrMin().getZ()+maxZDist );
		
		Extent e = bbox.extnt();
		e.setZ(zHigh-zLow);
		
		bbox.getCrnrMin().setZ( zLow );
		bbox.setExtnt(e);
				
		//System.out.printf("Extended box=%s\n", bbox.toString() );
		//System.out.printf("maxZDist=%d\tskipZDist=%d\t[%d,%f,%d]\n", maxZDist, skipZDist, bbox.getCrnrMin().getZ(),pntRoot.z,bbox.calcCrnrMax().getZ());

		if (chnlFilled==null) {
			return PointsFromBinaryChnl.pointsFromChnlInsideBox(chnl, bbox, (int) Math.floor(pntRoot.getZ()), skipZDist);
		}
		
		return pointsFromChnlInsideBoxConvexOnly(chnl, chnlFilled, bbox, pntRoot, pl, skipZDist);
	}
	
	
	
	// TODO horribly long, refactor
	private static List<Point3i> pointsFromChnlInsideBoxConvexOnly(
			BinaryChnl chnl,
			BinaryChnl chnlFilled,
			BoundingBox bbox,
			Point3d pntRoot,
			PointListForConvex pntsConvexRoot,
			int skipAfterSuccessiveEmptySlices
		) throws CreateException {

			assert( chnl.getDimensions().contains(bbox) );
		
			int startZ = (int) Math.floor(pntRoot.getZ());
			
			
			BinaryVoxelBox<ByteBuffer> binaryVoxelBox = chnlFilled.binaryVoxelBox();
			
			List<Point3i> listOut = new ArrayList<>();
			
			BinaryValuesByte bvb = chnl.getBinaryValues().createByte();
			
			VoxelBox<ByteBuffer> vb = chnl.getChnl().getVoxelBox().asByte();
			
			
			// Stays as -1 until we reach a non-empty slice
			int successiveEmptySlices = -1;
				
			Extent e = vb.extnt();
			Point3i crnrMin = bbox.getCrnrMin();
			Point3i crnrMax = bbox.calcCrnrMax();
			
			
			for( int z=startZ; z<=crnrMax.getZ(); z++) {
				
				ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
				
				boolean addedToSlice = false;
				for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
					for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
						
						int offset = e.offset(x, y);
						if (bb.get(offset)==bvb.getOnByte()) {
							
							Point3i pnt = new Point3i(x,y,z); 
							
							//System.out.printf("For %s: ",pnt);
							if( pntsConvexRoot.convexWithAtLeastOnePoint(pnt, binaryVoxelBox)) {
								//System.out.printf(" passed\n");
								addedToSlice = true;
								listOut.add( pnt );
							} else {
								//System.out.printf(" failed\n");
							}

						}
						
					}
				}
				
				if (!addedToSlice) {
					successiveEmptySlices = 0;
					
				// We don't increase the counter until we've been inside a non-empty slice
				} else if (successiveEmptySlices!=-1){	
					successiveEmptySlices++;
					if (successiveEmptySlices >= skipAfterSuccessiveEmptySlices) {
						break;
					}
					
				}
			}
			
			// Exit early if we start on the first slice
			if (startZ==0) {
				return listOut;
			}
			
			for( int z=(startZ-1); z>=crnrMin.getZ(); z--) {
				
				ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
				
				boolean addedToSlice = false;
				for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
					for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
						
						int offset = e.offset(x, y);
						if (bb.get(offset)==bvb.getOnByte()) {
							
							Point3i pnt = new Point3i(x,y,z);
							
							//System.out.printf("For %s: ",pnt);
							if( pntsConvexRoot.convexWithAtLeastOnePoint(pnt, binaryVoxelBox)) {
								addedToSlice = true;
								listOut.add( pnt );
							}
						}
						
					}
				}
				
				if (!addedToSlice) {
					successiveEmptySlices = 0;
					
				// We don't increase the counter until we've been inside a non-empty slice
				} else if (successiveEmptySlices!=-1){	
					successiveEmptySlices++;
					if (successiveEmptySlices >= skipAfterSuccessiveEmptySlices) {
						break;
					}
					
				}
			}
			
			
			return listOut;
		}

	
	
	
	private static BoundUnitless zSize( List<Point3i> pnts ) {
		
		boolean first = true;
		int min = 0;
		int max = 0;
		for( Point3i p : pnts) {
			if (first) {
				min = p.getZ();
				max = p.getZ();
				first = false;
			} else {
				if (p.getZ() <min) {
					min=p.getZ();
				}
				if (p.getZ()>max) {
					max=p.getZ();
				}				
			}
		}
		return new BoundUnitless(min,max);
	}
	
	
	private Point3i calcCOG( List<Point3i> pnts ) throws OperationFailedException {
		
		if (pnts.size()==0) {
			throw new OperationFailedException("There are no points in the list, so now center-of-gravity exists");
		}
		
		double sumX = 0.0;
		double sumY = 0.0;
		double sumZ = 0.0;
		int cnt= 0;
		for( Point3i p : pnts) {
			sumX += p.getX();
			sumY += p.getY();
			sumZ += p.getZ();
			cnt++;
		}
		
		int cogX = (int) Math.round(sumX/cnt);
		int cogY = (int) Math.round(sumY/cnt);
		int cogZ = (int) Math.round(sumZ/cnt);
		
		return new Point3i( cogX, cogY, cogZ  );
	}
	
	private List<Point3i> forceMinimumZSize( List<Point3i> pntsIn, BoundUnitless bound ) throws OperationFailedException {
		
		List<Point3i> pntsOut = new ArrayList<Point3i>();
		pntsOut.addAll(pntsIn);
		
		// How many extra do we need
		int boundSize = (int) Math.floor(bound.size());
		int boundMin = (int) Math.floor(bound.getMin());
		int boundMax = (int) Math.floor(bound.getMax());
		
		int extraNeeded = forceMinZSize - boundSize;
		
		int extraNeededLower = extraNeeded / 2;
		int extraNeededUpper = extraNeeded - extraNeededLower;
		
		int boundMinNew = boundMin - extraNeededLower;
		int boundMaxNew = boundMax + extraNeededUpper;
		
		
//		// We calculate a mean point
//		for( Point3i p : pntsIn) {
//			if (p.z==boundMin) {
//				pntsOut.add( new Point3i(p.x, p.y, z) );
//			}
//		}
//		
		
		
		Point3i cog = calcCOG(pntsIn);
		
//		double scale = 2;
//		
//		for( Point3i p : pntsIn) {
//
//			int zFromCog = p.z - cog.getZ();
//				
//			pntsOut.add( new Point3i(p.x, p.y, cog.getZ() + (int) (scale*zFromCog)) );
//			
//		}
		
		
		if (extraNeededLower!=0) {
			pntsOut.add( new Point3i(cog.getX(),cog.getY(),boundMinNew) );
		}
		
		if (extraNeededUpper!=0) {
			pntsOut.add( new Point3i(cog.getX(),cog.getY(),boundMaxNew) );
		}
		
		
//		for( int z=(boundMin-1); z>=boundMinNew; z--) {
//			// We add all the points with a new z
//			for( Point3i p : pntsIn) {
//				if (p.z==boundMin) {
//					
//					pntsOut.add( new Point3i(p.x, p.y, z) );
//				}
//			}
//		//pntsOut.add( new Point3i(cog.x,cog.y,z) );
//		}
		
		
//		for( int z=(boundMin-1); z>=boundMinNew; z--) {
//			// We add all the points with a new z
//			for( Point3i p : pntsIn) {
//				if (p.z==boundMin) {
//					
//					pntsOut.add( new Point3i(p.x, p.y, z) );
//				}
//			}
//			//pntsOut.add( new Point3i(cog.x,cog.y,z) );
//		}
//		
//		for( int z=(boundMin+1); z<=boundMaxNew; z++) {
//			// We add all the points with a new z
//			for( Point3i p : pntsIn) {
//				if (p.z==boundMax) {
//					pntsOut.add( new Point3i(p.x, p.y, z) );
//				}
//			}
//			//pntsOut.add( new Point3i(cog.x,cog.y,z) );
//		}		
		
		return pntsOut;
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return orientationXYProposer.isCompatibleWith(testMark);
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		CreateProposeVisualizationList list = new CreateProposeVisualizationList();
		list.add( pointsFromOrientationXYProposer.proposalVisualization(detailed) );
		
		list.add( new ICreateProposalVisualization() {
			
			@Override
			public void addToCfg(ColoredCfg cfg) {
				if (lastPntsAll!=null&& lastPntsAll.size()>0) {
					cfg.addChangeID( MarkPointListFactory.createMarkFromPoints3i(lastPntsAll), new RGBColor(Color.ORANGE) );
				}
			}
		});
		
		return list;
	}

	public OrientationProposer getOrientationXYProposer() {
		return orientationXYProposer;
	}

	public void setOrientationXYProposer(OrientationProposer orientationXYProposer) {
		this.orientationXYProposer = orientationXYProposer;
	}

	public PointsFromOrientationProposer getPointsFromOrientationXYProposer() {
		return pointsFromOrientationXYProposer;
	}

	public void setPointsFromOrientationXYProposer(
			PointsFromOrientationProposer pointsFromOrientationXYProposer) {
		this.pointsFromOrientationXYProposer = pointsFromOrientationXYProposer;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	public UnitValueDistance getDistanceZEndIfEmpty() {
		return distanceZEndIfEmpty;
	}

	public void setDistanceZEndIfEmpty(UnitValueDistance distanceZEndIfEmpty) {
		this.distanceZEndIfEmpty = distanceZEndIfEmpty;
	}

	public int getMinNumSlices() {
		return minNumSlices;
	}

	public void setMinNumSlices(int minNumSlices) {
		this.minNumSlices = minNumSlices;
	}

	public int getForceMinZSize() {
		return forceMinZSize;
	}

	public void setForceMinZSize(int forceMinZSize) {
		this.forceMinZSize = forceMinZSize;
	}

	public ScalarProposer getMaxDistanceZ() {
		return maxDistanceZ;
	}

	public void setMaxDistanceZ(ScalarProposer maxDistanceZ) {
		this.maxDistanceZ = maxDistanceZ;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProviderFilled() {
		return binaryImgChnlProviderFilled;
	}

	public void setBinaryImgChnlProviderFilled(
			BinaryImgChnlProvider binaryImgChnlProviderFilled) {
		this.binaryImgChnlProviderFilled = binaryImgChnlProviderFilled;
	}

	public boolean isForwardDirectionOnly() {
		return forwardDirectionOnly;
	}

	public void setForwardDirectionOnly(boolean forwardDirectionOnly) {
		this.forwardDirectionOnly = forwardDirectionOnly;
	}
}
