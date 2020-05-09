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
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
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
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.PointsFromOrientationProposer;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;


public class XYOrientationExtendToZ extends PointsProposer {
	
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
	
	@Override
	public Optional<List<Point3i>> propose(Point3d pnt, Mark mark, ImageDim dim, RandomNumberGenerator re, ErrorNode errorNode) {
		pointsFromOrientationXYProposer.clearVisualizationState();
		Optional<Orientation> orientation = orientationXYProposer.propose(
			mark,
			dim,
			re,
			errorNode.add("orientationProposer")
		);
		
		return orientation.flatMap( or->
			proposeFromOrientation(or, pnt, dim, re, errorNode)
		);
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		
		CreateProposeVisualizationList list = new CreateProposeVisualizationList();
		list.add( pointsFromOrientationXYProposer.proposalVisualization(detailed) );

		list.add( cfg -> {
			if (lastPntsAll!=null && lastPntsAll.size()>0) {
				cfg.addChangeID(
					MarkPointListFactory.createMarkFromPoints3i(lastPntsAll),
					new RGBColor(Color.ORANGE)
				);
			}
		});
		return list;
	}
	
	private Optional<List<Point3i>> proposeFromOrientation(Orientation orientation, Point3d pnt, ImageDim dim, RandomNumberGenerator re, ErrorNode errorNode) {
		try {
			List<List<Point3i>> pntsXY = getPointsFromOrientationXYProposer().calcPoints( pnt, orientation, dim.getZ()>1, re, forwardDirectionOnly );

			lastPntsAll = GeneratePointsHelper.generatePoints(
				pnt,
				pntsXY,
				maxZDist(re, dim.getRes()),
				skipZDist(dim.getRes()),
				binaryImgChnlProvider.create(),
				chnlFilled(),
				dim,
				forceMinZSize
			);
			return Optional.of(lastPntsAll);
			
		} catch (CreateException | OperationFailedException | TraverseOutlineException e1) {
			errorNode.add(e1);
			return Optional.empty();
		}
	}
	
	private int maxZDist(RandomNumberGenerator re, ImageRes res) throws OperationFailedException {
		int maxZDist = (int) Math.round(
			maxDistanceZ.propose(re, res)
		);
		maxZDist = Math.max(maxZDist, minNumSlices);
		return maxZDist;
	}
	
	private int skipZDist(ImageRes res) {
		int skipZDist = (int) Math.round(distanceZEndIfEmpty.rslv(res, new DirectionVector(0, 0, 1.0)) );
		// TODO fix. Why this constant?
		skipZDist = 100;
		return skipZDist;
	}
	
	private Optional<BinaryChnl> chnlFilled() throws CreateException {
		return binaryImgChnlProviderFilled!=null ? Optional.of(binaryImgChnlProviderFilled.create()) : Optional.empty();
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return orientationXYProposer.isCompatibleWith(testMark);
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
}
