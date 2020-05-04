package org.anchoranalysis.plugin.image.merge.priority;

/*
 * #%L
 * anchor-image-feature
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


import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.image.merge.ObjVertex;


/**
 * Allows merges if there is an increase in the payload i.e. <pre>if payload(merged) >= avg( payload(src), payload(dest )</pre>
 * 
 * <p>Prioritises merges in order of greatest improvement.</p>
 * 
 * @author Owen Feehan
 *
 */
public class AssignPriorityFromImprovement extends AssignPriority {

	private PayloadCalculator payloadCalculator;
	
	public AssignPriorityFromImprovement(PayloadCalculator payloadCalculator) {
		super();
		this.payloadCalculator = payloadCalculator;
	}

	@Override
	public PrioritisedVertex assignPriorityToEdge(
		ObjVertex src,
		ObjVertex dest,
		ObjMask merged,
		ErrorReporter errorReporter
	) throws OperationFailedException
	{
		double payloadMerge = calcPayload(payloadCalculator, merged);
		double payloadExisting = weightedAverageFeatureVal(src, dest);
		double improvement = payloadMerge - payloadExisting;
		
		if (payloadMerge<=payloadExisting) {	
			// We add an edge anyway, as a placeholder as future merges might make it viable
			return new PrioritisedVertex(merged,payloadMerge,improvement,false);
		} 
		
		return new PrioritisedVertex(merged,payloadMerge,improvement,true);
	}
	
	private double calcPayload( PayloadCalculator payloadCalculator, ObjMask om ) throws OperationFailedException {
		try {
			return payloadCalculator.calc(om);
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
	
	/** A weighted-average of the feature value (weighted according to the relative number of pixeks */
	private static double weightedAverageFeatureVal( ObjVertex om1, ObjVertex om2 ) {
		long size1 = om1.numPixels();
		long size2 = om2.numPixels();
		
		double weightedSum = (om1.getPayload()*size1) + (om2.getPayload()*size2);
		return weightedSum / (size1 + size2);
	}
}