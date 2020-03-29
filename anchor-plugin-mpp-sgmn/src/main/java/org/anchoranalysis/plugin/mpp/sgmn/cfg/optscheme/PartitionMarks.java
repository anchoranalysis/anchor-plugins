package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

public class PartitionMarks<T> {

	private Set<T> available;
	private Set<T> accepted;
	private Function<T,Double> funcExtractWeight;
	private EnumeratedDistribution<T> dist;
	
	public PartitionMarks(List<T> all, Function<T,Double> funcExtractWeight ) {
		super();
		
		this.available = setFromList( all );
		this.accepted = new HashSet<>();
		this.funcExtractWeight = funcExtractWeight;
		updateDist();
	}

	/**
	 * Samples from the available marks
	 * 
	 * @param propContext
	 * @param k sample-size
	 * 
	 * @return the sampled marks or NULL if none are available
	 */
	public boolean sampleAvailable( ProposerContext propContext, int k, T[] sampleArr ) {
		
		if (dist==null) {
			return false;
		}
		
		dist.sample(k, sampleArr);
		return true;
	}
	
	public void moveAvailableToAccepted( Set<T> list ) {
		for( T item : list ) {
			moveAvailableToAccepted(item);
		}
		updateDist();
	}
	
	private void moveAvailableToAccepted( T pxlMark ) {
		assert( available.contains(pxlMark) );
		available.remove( pxlMark );
		accepted.add(pxlMark);
	}
	
	public void moveAcceptedToAvailable( T pxlMark ) {
		assert( accepted.contains(pxlMark) );
		accepted.remove( pxlMark );
		available.add(pxlMark);
		updateDist();
	}
	
	private void updateDist() {

		// Early exit when nothing is available
		if (available.size()==0) {
			dist = null;
			return;
		}
		
		List<Pair<T,Double>> list = new ArrayList<>();
		for( T item : available ) {
			Pair<T,Double> pair = new Pair<>(
				item,
				funcExtractWeight.apply(item)
			);
			list.add(pair);
		}

		dist = new EnumeratedDistribution<T>(list);
	}
	
	private static <T> Set<T> setFromList( List<T> allMarks ) {
		return new HashSet<>(allMarks);
	}
}
