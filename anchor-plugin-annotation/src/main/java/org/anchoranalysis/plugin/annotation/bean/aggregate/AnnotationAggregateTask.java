package org.anchoranalysis.plugin.annotation.bean.aggregate;

/*-
 * #%L
 * anchor-plugin-annotation
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

import java.util.List;

import org.anchoranalysis.annotation.io.bean.strategy.AnnotatorStrategy;
import org.anchoranalysis.annotation.io.input.AnnotationWithStrategy;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/**
 * Aggregates many per-image annotations together in form of a CSV file
 * 
 * @author FEEHANO
 *
 */
public class AnnotationAggregateTask<S extends AnnotatorStrategy> extends Task<AnnotationWithStrategy<S>,AggregateSharedState> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public AggregateSharedState beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		try {
			return new AggregateSharedState();
		} catch (CreateException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	public void doJobOnInputObject(ParametersBound<AnnotationWithStrategy<S>, AggregateSharedState> params)
			throws JobExecutionException {
			
		ImageAnnotation ann = createFromInputObject( params.getInputObject() );
		
		if (ann!=null) {
			params.getSharedState().getAnnotations().add(ann);
		}
	}
	
	@Override
	public void afterAllJobsAreExecuted(BoundOutputManagerRouteErrors outputManager, AggregateSharedState sharedState,
			LogReporter logReporter) throws ExperimentExecutionException {

		outputManager.getWriterCheckIfAllowed().write(
			"annotationsAgg",
			() -> createGenerator( sharedState.getAnnotations() )
		);
	}
	
	
	private static AnnotationAggregateCSVGenerator createGenerator( List<ImageAnnotation> annotations ) {
		AnnotationAggregateCSVGenerator generator = new AnnotationAggregateCSVGenerator();
		generator.setIterableElement( annotations );
		return generator;
	}
	
	private ImageAnnotation createFromInputObject( AnnotationWithStrategy<S> inputObject ) throws JobExecutionException {
		try {
			String label = inputObject.labelForAggregation();
			
			// There is no label
			if (label==null) {
				return null;
			}
			
			return new ImageAnnotation(
				inputObject.descriptiveName(),
				label		
			);
		} catch (AnchorIOException exc) {
			throw new JobExecutionException(exc);
		}			
	}

	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(AnnotationWithStrategy.class);
	}
}
