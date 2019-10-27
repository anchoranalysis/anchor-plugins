package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

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

import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;

// Breadth-first iteration of pixels
public class VisitSchedulerMaxDistZ extends VisitScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private ScalarProposer maxDistProposer;
	// END BEAN PROPERTIES
	
	private Point3i root;
	private double maxZRslv;
	
	public VisitSchedulerMaxDistZ() {
		super();
	}
	
	@Override
	public void beforeCreateObjMask(RandomNumberGenerator re, ImageRes res)
			throws InitException {
		try {
			maxZRslv = maxDistProposer.propose(re, res);
		} catch (OperationFailedException e) {
			throw new InitException(e);
		}
		//System.out.printf("Z rslv %f\n", maxZRslv);
	}
	
	@Override
	public Tuple3i maxDistFromRootPoint(ImageRes res) {
		int distZ = (int) Math.ceil(maxZRslv);
		
		// Arbitrarily large numbers
		return new Point3i(10000000,10000000,distZ);
	}
	
	@Override
	public void afterCreateObjMask(Point3i root, ImageRes res, RandomNumberGenerator re) {
		this.root = root;
	}

	@Override
	public boolean considerVisit( Point3i pnt, int distAlongContour, ObjMask objMask ) {
		System.out.printf("root=%d,%d,%d   pnt=%d,%d,%d\n", root.getX(), root.getY(), root.getZ(), pnt.getX(), pnt.getY(), pnt.getZ() );
		if (Math.abs(root.getZ()-pnt.getZ())>maxZRslv) {
			return false;
		}
		
		return true;
	}

	public ScalarProposer getMaxDistProposer() {
		return maxDistProposer;
	}

	public void setMaxDistProposer(ScalarProposer maxDistProposer) {
		this.maxDistProposer = maxDistProposer;
	}

}
