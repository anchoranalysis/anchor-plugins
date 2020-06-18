package org.anchoranalysis.plugin.mpp.bean.proposer.radii;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.bound.Bound;

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

import org.anchoranalysis.anchor.mpp.bean.proposer.radii.RadiiProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.anchor.mpp.mark.conic.RadiiRandomizer;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;

public class UniformRandomRadiiProposer extends RadiiProposer {

	// START BEAN PROPERTIES
	@BeanField
	private Bound radiusBound;
	
	@BeanField
	private boolean do3D = false;
	// END BEAN PROPERTIES

	public UniformRandomRadiiProposer() {
		// Standard Bean Constructor
	}
	
	public UniformRandomRadiiProposer(Bound radiusBound, boolean do3D) {
		this.radiusBound = radiusBound;
		this.do3D = do3D;
	}
	
	@Override
	public Optional<Point3d> propose(
		Point3d pos,
		RandomNumberGenerator randomNumberGenerator,
		ImageDim dim,
		Orientation orientation
	) throws ProposalAbnormalFailureException {
		return Optional.of(
			RadiiRandomizer.randomizeRadii(
				radiusBound,
				randomNumberGenerator,
				dim.getRes(),
				do3D
			)
		);
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkConic;
	}
	
	public Bound getRadiusBound() {
		return radiusBound;
	}

	public void setRadiusBound(Bound radiusBound) {
		this.radiusBound = radiusBound;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}
}
