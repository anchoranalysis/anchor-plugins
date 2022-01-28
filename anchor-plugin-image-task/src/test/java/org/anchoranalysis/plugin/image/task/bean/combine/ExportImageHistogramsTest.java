package org.anchoranalysis.plugin.image.task.bean.combine;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.anchoranalysis.plugin.image.task.bean.grouped.histogram.ExportImageHistograms;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ExportImageHistograms}.
 * 
 * @author Owen Feehan
 */
class ExportImageHistogramsTest extends GroupedStackTestBase {

    private static List<String> FILENAMES_TO_COMPARE =
            Arrays.asList("stack00.csv", "stack01.csv", "stack02.csv");
    
    /** 
     * Do <b>not</b> resize the input images. 
     * 
     * <p>This means the input images have different sizes.
     */
	@Test
	void testDoNotResize() throws OperationFailedException, ImageIOException {
		doTest(false, Optional.empty());
	}

	@Override
	protected GroupedStackBase<?, ?> createTask() {
		return new ExportImageHistograms();
	}

	@Override
	protected Iterable<String> filenamesToCompare() {
		return FILENAMES_TO_COMPARE;
	}

	@Override
	protected String subdirectoryResized() {
		return "exportImageHistograms/expectedOutputResized/";
	}

	@Override
	protected String subdirectoryNotResized() {
		return "exportImageHistograms/expectedOutputNotResized/";
	}
}
