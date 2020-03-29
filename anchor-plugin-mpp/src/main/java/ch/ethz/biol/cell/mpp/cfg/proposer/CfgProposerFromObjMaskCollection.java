package ch.ethz.biol.cell.mpp.cfg.proposer;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;

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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.EllipsoidFactory;

public class CfgProposerFromObjMaskCollection extends CfgProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private boolean suppressZCovariance = false;
	
	@BeanField
	private double shellRad = 0.2;
	
	@BeanField @Optional
	private CheckMark checkMark = null;
	// END BEAN PROPERTIES
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipsoid;
	}

	private MarkEllipsoid createFromObjMask( ObjMask om, NRGStackWithParams nrgStack ) throws CreateException {
		MarkEllipsoid me = EllipsoidFactory.createMarkEllipsoidLeastSquares( om , nrgStack.getDimensions(), suppressZCovariance, shellRad );
		return me;
	}
	
	@Override
	public Cfg propose(CfgGen cfgGen, ProposerContext context) throws ProposalAbnormalFailureException {


		ObjMaskCollection objsCollection;
		try {
			objsCollection = objs.create();
		} catch (CreateException e) {
			throw new ProposalAbnormalFailureException("Failed to create objs", e);
		}
			
		Cfg cfg = new Cfg();
				
		try {
			if (checkMark!=null) {
				checkMark.start(context.getNrgStack());
			}
		} catch (OperationFailedException e) {
			throw new ProposalAbnormalFailureException("Failed to start checkMark", e);
		}

		try {
			for( ObjMask om : objsCollection ) {
				Mark mark = createFromObjMask(om, context.getNrgStack());
				mark.setId( cfgGen.idAndIncrement() );
				
				if (checkMark!=null) {
					if (!checkMark.check(mark, context.getRegionMap(), context.getNrgStack())) {
						continue;
					}
				}
				
				cfg.add( mark );
			}
			
			if (checkMark!=null) {
				checkMark.end();
			}
			
			return cfg;
			
		} catch (CreateException e) {
			throw new ProposalAbnormalFailureException("Failed to create a mark from the obj-mask", e);
		} catch (CheckException e) {
			throw new ProposalAbnormalFailureException("A failure occurred while checking the mark", e);
		}
	}

	public boolean isSuppressZCovariance() {
		return suppressZCovariance;
	}

	public void setSuppressZCovariance(boolean suppressZCovariance) {
		this.suppressZCovariance = suppressZCovariance;
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public double getShellRad() {
		return shellRad;
	}

	public void setShellRad(double shellRad) {
		this.shellRad = shellRad;
	}

	public CheckMark getCheckMark() {
		return checkMark;
	}

	public void setCheckMark(CheckMark checkMark) {
		this.checkMark = checkMark;
	}
}
