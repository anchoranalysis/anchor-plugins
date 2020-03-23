package ch.ethz.biol.cell.mpp.mark.proposer.points;

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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.outline.FindOutline;
import org.anchoranalysis.image.points.PointsFromBinaryVoxelBox;

public class ObjMaskContour extends PointsProposer {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}

	@Override
	public List<Point3i> propose(Point3d pnt, Mark mark, ImageDim dim,
			RandomNumberGenerator re, ErrorNode errorNode) {

		ObjMaskCollection objsCollection;
		try {
			objsCollection = objs.create();
		} catch (CreateException e) {
			errorNode.add(e);
			return null;
		}
		
		int index = (int) (re.nextDouble() * objsCollection.size() );
		
		ObjMask om = objsCollection.get(index);

		List<Point3i> listPnts = new ArrayList<>();
		
		
		try {
			ObjMask outline = FindOutline.outline(om, 1, false, true);
			
			PointsFromBinaryVoxelBox.addPointsFromVoxelBox3D( outline.binaryVoxelBox(), outline.getBoundingBox().getCrnrMin(), listPnts );
		} catch (CreateException e) {
			errorNode.add(e);
			return null;
		}

		return listPnts;
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return null;
	}


	public ObjMaskProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}
}
