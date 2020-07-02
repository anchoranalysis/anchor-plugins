package ch.ethz.biol.cell.mpp.mark.proposer;

/*
 * #%L
 * anchor-plugin-points
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
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.log.LogErrorReporter;

public class MarkProposerPointsFitter extends MarkProposer {

	// START BEAN PROPERTIES
	@BeanField
	private PointsProposer pointsProposer;
	
	@BeanField
	private PointsFitter pointsFitter;
	
	@BeanField
	private boolean reportFitterErrors = true;
	// END BEAN PROPERTIES
	
	@SuppressWarnings("unused")
	private LogErrorReporter logErrorReporter;
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return pointsFitter.isCompatibleWith(testMark) && pointsProposer.isCompatibleWith(testMark);
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) throws ProposalAbnormalFailureException {
		
		inputMark.reset();
		
		Point3d pnt = inputMark.getMark().centerPoint();
		
		try {
			Optional<List<Point3i>> pnts = pointsProposer.propose(
				pnt,
				inputMark.getMark(),
				context.getDimensions(),
				context.getRandomNumberGenerator(),
				context.getErrorNode().add("pointsProposer")
			);
			
			if (!pnts.isPresent()) {
				return false;	
			}
			
			// Now we create a list of point2d, and run the ellipse fitter on these
			ArrayList<Point3f> fitList = new ArrayList<>();
			for( Point3i p : pnts.get()) {
				fitList.add(
					PointConverter.floatFromInt(p)
				);
			}

			pointsFitter.fit(fitList, inputMark.getMark(), context.getDimensions());
			
		} catch (PointsFitterException | InsufficientPointsException e) {
			
			if (reportFitterErrors) {
				getLogger().getErrorReporter().recordError(MarkProposerPointsFitter.class, e);
			}
			context.getErrorNode().add( e.toString() );
			return false;
		}
		return true;
	}
	
	@Override
	public Optional<CreateProposalVisualization> proposalVisualization(final boolean detailed) {
		return Optional.of(	cfg -> {
			CreateProposalVisualization pv = pointsProposer.proposalVisualization(detailed); 
			if (pv!=null) {
				pv.addToCfg(cfg);
			}
		});
	}
	
	public PointsProposer getPointsProposer() {
		return pointsProposer;
	}

	public void setPointsProposer(PointsProposer pointsProposer) {
		this.pointsProposer = pointsProposer;
	}

	public boolean isReportFitterErrors() {
		return reportFitterErrors;
	}

	public void setReportFitterErrors(boolean reportFitterErrors) {
		this.reportFitterErrors = reportFitterErrors;
	}

	public PointsFitter getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitter pointsFitter) {
		this.pointsFitter = pointsFitter;
	}
}
