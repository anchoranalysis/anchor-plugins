package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import java.io.IOException;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramArray;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;

class GroupedHistogramMap extends GroupMapByName<Histogram,Histogram> {
	
	private final GroupedHistogramWriter writer;
	
	public GroupedHistogramMap(GroupedHistogramWriter writer, int maxValue) {
		super(
			"histogram",
			() -> new HistogramArray( maxValue )
		);
		this.writer = writer;
	}

	@Override
	protected void addTo(Histogram ind, Histogram agg) throws OperationFailedException {
		agg.addHistogram(ind);
	}

	@Override
	protected void writeGroupOutputInSubdirectory(
		String outputName,
		Histogram agg,
		ConsistentChannelChecker chnlChecker,
		BoundIOContext context
	) throws IOException {
		writer.writeHistogramToFile(
			agg,
			outputName,
			context
		);
	}
}