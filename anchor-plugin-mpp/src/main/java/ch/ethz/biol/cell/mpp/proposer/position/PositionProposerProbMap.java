package ch.ethz.biol.cell.mpp.proposer.position;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;

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

import org.anchoranalysis.anchor.mpp.bean.proposer.PositionProposerBean;
import org.anchoranalysis.anchor.mpp.probmap.ProbMap;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;

public class PositionProposerProbMap extends PositionProposerBean {

	//private static Log log = LogFactory.getLog(PositionProposer.class);
	//private boolean first = true;
	
	// BEAN PROPERTIES
	@BeanField
	private String probMapID = "";
	// END BEAN PROPERTIES
	
	private ProbMap probMap = null;	
	
	@Override
	public void onInit(MPPInitParams pso) throws InitException {
		super.onInit(pso);
		try {
			this.probMap = getSharedObjects().getProbMapSet().getException(probMapID);
		} catch (NamedProviderGetException e) {
			throw new InitException(e.summarize());
		}
	}
	
	@Override
	public Point3d propose(ProposerContext context) {
		
		// TODO probMap can fail, be able to handle this circumstance
		Point3d pnt = probMap.sample( context.getRe() );
		
		if (pnt==null) {
			return null;
		}
		
		assert( pnt.getX() >= 0 );
		assert( pnt.getY() >= 0 );
		assert( pnt.getZ() >= 0 );
		
		assert( pnt.getX() < context.getDimensions().getX() );
		assert( pnt.getY() < context.getDimensions().getY() );
		assert( pnt.getZ() < context.getDimensions().getZ() );
		
		return pnt;
	}

	public String getProbMapID() {
		return probMapID;
	}

	public void setProbMapID(String probMapID) {
		this.probMapID = probMapID;
	}
}
