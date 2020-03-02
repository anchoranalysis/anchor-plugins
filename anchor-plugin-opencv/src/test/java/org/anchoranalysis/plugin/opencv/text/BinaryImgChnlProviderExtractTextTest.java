package org.anchoranalysis.plugin.opencv.text;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.stack.StackProviderHolder;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.TestLoaderImageIO;
import org.junit.Test;

public class BinaryImgChnlProviderExtractTextTest {

	private TestLoaderImageIO testLoader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	@Test
	public void testSimple() throws AnchorIOException, CreateException {
		
			Stack stack = testLoader.openStackFromTestPath("car.jpg");
		
			BinaryImgChnlProviderExtractText provider = new BinaryImgChnlProviderExtractText();
			provider.setStackProvider( new StackProviderHolder(stack) );
			
			@SuppressWarnings("unused")
			BinaryChnl bc = provider.create();
			
	}
}
