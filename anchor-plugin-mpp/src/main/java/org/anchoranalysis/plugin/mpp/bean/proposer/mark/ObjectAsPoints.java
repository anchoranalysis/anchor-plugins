package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

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
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointList;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObjMask;

import lombok.Getter;
import lombok.Setter;

public class ObjectAsPoints extends MarkProposer {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objects;
	// END BEAN PROPERTIES

	private List<List<Point3d>> points = null;
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}

	@Override
	public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context) throws ProposalAbnormalFailureException {

		try {
			createObjsIfNecessary();
		} catch (CreateException e) {
			context.getErrorNode().add(e);
			return false;
		}
		
		MarkPointList mark = MarkPointListFactory.create(
			// Selects an object randomly
			randomlySelectPoints(context)
		);
		
		inputMark.assignFrom(mark);
		inputMark.reset();
		
		return true;
	}
	
	private List<Point3d> randomlySelectPoints( ProposerContext context ) {
		int selectedIndex = (int) (context.getRandomNumberGenerator().nextDouble() * points.size());
		return points.get(selectedIndex);
	}

	@Override
	public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
		return Optional.empty();
	}
	
	private void createObjsIfNecessary() throws CreateException {
		if (points==null) {
			points = new ArrayList<>();
			
			for( ObjectMask object : objects.create()) {
				points.add(
					PointsFromObjMask.pntsFromMaskDouble(object)
				);
			}
		}
	}
}
