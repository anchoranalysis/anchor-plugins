package ch.ethz.biol.cell.mpp.mark.proposer.points;

/*-
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.image.points.PointsFromObjMask;

/**
 * 1. Iterates over each object
 * 2. For each object, with a random probability of 0.5, includes all pixels inside the object
 * 
 * @author feehano
 *
 */
public class IncludeRandomObjsPointsProposer extends PointsProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objMaskProvider;
	// END BEAN PROPERTIES
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}

	@Override
	public List<Point3i> propose(Point3d pnt, Mark mark, ImageDim dim, RandomNumberGenerator re,
			ErrorNode errorNode) {

		List<Point3i> out = new ArrayList<>();
		
		try {
			ObjMaskCollection objs = objMaskProvider.create();
			
			for( ObjMask om : objs ) {
				maybeAddToList(om, out, re);
			}
						
		} catch (CreateException e) {
			errorNode.add(e);
		}
		
		return out;
	}
	
	private static void maybeAddToList( ObjMask om, List<Point3i> out, RandomNumberGenerator re ) throws CreateException {
		if (re.nextDouble() > 0.5) {
			out.addAll(
				PointsFromObjMask.pntsFromMask(om)
			);
		}
	}
	

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return null;
	}

	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}


	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}
}
