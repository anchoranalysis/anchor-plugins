package org.anchoranalysis.plugin.image.task.bean.grouped.raster;







import java.util.Optional;

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
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedSharedState;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackTask;
import org.anchoranalysis.plugin.image.task.grouped.ChnlSource;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChnlChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMap;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;


/**
 * Creates a an aggregated-image for each group, where each voxel-value is the mean voxel-value across the group.
 * 
 * @author Owen Feehan
  */
public class GroupedMeanChnlTask extends GroupedStackTask<Chnl,AggregateChnl> {

	@Override
	protected GroupMap<Chnl, AggregateChnl> createGroupMap(ConsistentChnlChecker chnlChecker) {
		return new GroupedMeanChnlMap();
	}
	
	@Override
	protected void processKeys(
		NamedImgStackCollection store,
		Optional<String> groupName,
		GroupedSharedState<Chnl,AggregateChnl> sharedState,
		BoundIOContext context
	) throws JobExecutionException {

		ChnlSource source = new ChnlSource(
			store,
			sharedState.getChnlChecker()
		);
		
		try {
			for( NamedChnl chnl : getSelectChnls().selectChnls(source, true)) {
				sharedState.getGroupMap().add(
					groupName,
					chnl.getName(),
					chnl.getChnl()
				);	
			}
			
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	protected Optional<String> subdirectoryForGroupOutputs() {
		return Optional.empty();
	}
}
