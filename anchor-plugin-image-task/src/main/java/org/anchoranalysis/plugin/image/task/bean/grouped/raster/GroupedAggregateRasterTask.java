package org.anchoranalysis.plugin.image.task.bean.grouped.raster;







/*
 * #%L
 * anchor-plugin-image-task
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
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.grouped.BoundContextIntoSubfolder;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedSharedState;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackTask;
import org.anchoranalysis.plugin.image.task.grouped.ChnlSource;
import org.anchoranalysis.plugin.image.task.grouped.GroupMap;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;


/** Calculates feature on a 'grouped' set of images

   
**/
public class GroupedAggregateRasterTask extends GroupedStackTask<Chnl,AggregateChnl> {

	
	
	// START BEAN PROPERTIES
	// END BEAN PROPERTIES

	@Override
	public GroupedSharedState<Chnl,AggregateChnl> beforeAnyJobIsExecuted(
		BoundOutputManagerRouteErrors outputManager,
		ParametersExperiment params
	) throws ExperimentExecutionException {
		
		class GroupMapChnl extends GroupMap<Chnl,AggregateChnl> {
			
			public GroupMapChnl() {
				super(
					"chnl",
					() -> new AggregateChnl(),
					(indItem,aggItem) -> aggItem.addChnl(indItem)
				);
			}
		}
		
		return new GroupedSharedState<Chnl,AggregateChnl>(
			chnlChecker -> new GroupMapChnl() 	
		);
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	@Override
	protected void processKeys(
		NamedImgStackCollection store,
		String groupName,
		GroupedSharedState<Chnl,AggregateChnl> sharedState,
		BoundIOContext context
	) throws JobExecutionException {

		ChnlSource source = new ChnlSource(
			store,
			sharedState.getChnlChecker()
		);
		
		try {
			for( NamedChnl chnl : getSelectChnls().selectChnls(source, true)) {
				sharedState.getGroupMap().addToGroup(
					new CombinedName(
						groupName,
						chnl.getName()
					),
					chnl.getChnl()
				);	
			}
			
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
			
	@Override
	public void afterAllJobsAreExecuted( GroupedSharedState<Chnl,AggregateChnl> sharedState, BoundIOContext context) throws ExperimentExecutionException {
		
		ChnlGenerator generator = new ChnlGenerator("meanChnl");
		
		for( CombinedName group : sharedState.getGroupMap().keySet() ) {
			
			AggregateChnl agg = sharedState.getGroupMap().get(group);
			assert( agg!=null);
	
			writeGroup(
				generator,
				agg,
				group.getCombinedName(),
				sharedState.getChnlChecker().getChnlType(),
				new BoundContextIntoSubfolder(context, "stackCollection")
			);
		}	
	}
	
	private static void writeGroup(
		ChnlGenerator generator,
		AggregateChnl agg,
		String outputName,
		VoxelDataType outputType,
		BoundIOContext context
	) throws ExperimentExecutionException {
		try {
			Chnl mean = agg.createMeanChnl( outputType );
			generator.setIterableElement(mean);
			context.getOutputManager().getWriterAlwaysAllowed().write(
				outputName,
				() -> generator
			);
			
			context.getLogReporter().logFormatted(
				"Writing chnl %s with %d items and numPixels>100=%d and outputType=%s",
				outputName,
				agg.getCnt(),
				mean.getVoxelBox().any().countGreaterThan(100),
				outputType
			);
			
		} catch (OperationFailedException e) {
			throw new ExperimentExecutionException(
				String.format("Cannot create mean channel for: %s", outputName),
				e
			);
		}
	}
}
