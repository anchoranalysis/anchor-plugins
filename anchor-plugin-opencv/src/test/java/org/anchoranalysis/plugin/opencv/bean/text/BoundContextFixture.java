package org.anchoranalysis.plugin.opencv.bean.text;

import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.test.LoggingFixture;

import static org.mockito.Mockito.*;

public class BoundContextFixture {

	private BoundContextFixture() {}
	
	public static BoundIOContext withSimpleLogger() {
		
		LogErrorReporter logger = LoggingFixture.simpleLogErrorReporter();
		
		BoundIOContext out = mock(BoundIOContext.class);
		when(out.getLogger()).thenReturn(logger);
		when(out.getLogReporter()).thenReturn(logger.getLogReporter());
		when(out.getErrorReporter()).thenReturn(logger.getErrorReporter());
		return out;
	}
}
