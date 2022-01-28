package org.anchoranalysis.plugin.image.task.bean.combine;

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.plugin.image.bean.channel.aggregator.MeanProjection;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link AggregateChannelTask}.
 * 
 * @author Owen Feehan
 *
 */
class AggregateChannelTaskTest extends GroupedStackTestBase {

	private static final String OUTPUT_NAME = "someOutput";
	
    private static List<String> FILENAMES_TO_COMPARE =
            Arrays.asList("stack00.png", "stack01.png", "stack02.png");

	
    /** 
     * Do <b>not</b> resize the input images. 
     * 
     * <p>This means the input images have different sizes.
     */
	@Test
	void testDoNotResize() throws OperationFailedException, ImageIOException {
		assertThrows(OperationFailedException.class, () ->
			doTest(false, Optional.empty())
		);
	}

	@Override
	protected GroupedStackBase<?, ?> createTask() {
		AggregateChannelTask task = new AggregateChannelTask();
		task.setAggregator(new MeanProjection<>());
		task.setOutputName(OUTPUT_NAME);
		return task;
	}

	@Override
	protected Iterable<String> filenamesToCompare() {
		return FILENAMES_TO_COMPARE;
	}

	@Override
	protected String subdirectoryResized() {
		return "aggregateChannel/expectedOutput/";
	}

	@Override
	protected String subdirectoryNotResized() {
		// This is irrelevant, as any paths that lead here throw an exception first.
		return "aggregateChannel/expectedOutput/";
	}
}
