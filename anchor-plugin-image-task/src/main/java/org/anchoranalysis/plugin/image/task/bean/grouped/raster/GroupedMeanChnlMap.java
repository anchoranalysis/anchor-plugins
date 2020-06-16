package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import java.io.IOException;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;

class GroupedMeanChnlMap extends GroupMapByName<Channel,AggregateChnl> {
	
	public GroupedMeanChnlMap() {
		super(
			"chnl",
			AggregateChnl::new
		);
	}

	@Override
	protected void addTo(Channel ind, AggregateChnl agg) throws OperationFailedException {
		agg.addChnl(ind);
	}

	@Override
	protected void writeGroupOutputInSubdirectory(
		String outputName,
		AggregateChnl agg,
		ConsistentChannelChecker chnlChecker,
		BoundIOContext context
	) throws IOException {

		ChnlGenerator generator = new ChnlGenerator("meanChnl");
		
		VoxelDataType outputType = chnlChecker.getChnlType();
		
		try {
			Channel mean = agg.createMeanChnl( outputType );
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
			throw new IOException(
				String.format("Cannot create mean channel for: %s", outputName),
				e
			);
		}
	}
}