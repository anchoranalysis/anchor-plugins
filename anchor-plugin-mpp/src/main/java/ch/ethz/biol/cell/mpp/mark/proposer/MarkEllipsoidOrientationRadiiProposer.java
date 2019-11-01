package ch.ethz.biol.cell.mpp.mark.proposer;

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

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.EllipsoidBounds;
import org.anchoranalysis.anchor.mpp.mark.conic.EllipsoidRandomizer;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.image.orientation.Orientation;

public class MarkEllipsoidOrientationRadiiProposer extends MarkProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2076730247530509078L;

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipsoid;
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) {

		EllipsoidBounds markBounds;
		try {
			markBounds = (EllipsoidBounds) getSharedObjects().getMarkBounds();
		} catch (GetOperationFailedException e) {
			context.getErrorNode().add(e);
			return false;
		}
				
		MarkEllipsoid mark = (MarkEllipsoid) inputMark.getMark();
		inputMark.reset();
		
		Orientation orientation = EllipsoidRandomizer.randomizeRot(
			markBounds,
			context.getRe(),
			context.getDimensions().getRes()
		);
		
		Point3d radii = EllipsoidRandomizer.randomizeRadii(
			markBounds,
			context.getRe(),
			context.getDimensions().getRes()
		);
		
		mark.setMarksExplicit( mark.getPos(), orientation, radii);
		
		return true;
	}
	
	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return null;
	}
}
