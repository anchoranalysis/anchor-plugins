package org.anchoranalysis.plugin.opencv.text;

import static org.mockito.Mockito.mock;

import java.nio.file.Path;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenneConstant;
import org.anchoranalysis.image.bean.provider.stack.StackProviderHolder;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.TestLoaderImageIO;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ObjMaskProviderExtractTextTest {

	private TestLoaderImageIO testLoader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	@Test
	public void testSimple() throws AnchorIOException, CreateException, InitException {
		
		Stack stack = testLoader.openStackFromTestPath("car.jpg");
		
		ObjMaskProviderExtractText provider = createInitProvider(
			testLoader.getTestLoader().getRoot()
		);
		provider.setStackProvider( new StackProviderHolder(stack) );
		
		ObjMaskCollection objs = provider.create();
		
		assertTrue( objs.size()==2 );
	}
	
	private ObjMaskProviderExtractText createInitProvider( Path modelDir ) throws InitException {
		
		LogReporter logReporter = mock(LogReporter.class);
				
		LogErrorReporter logger = new LogErrorReporter(logReporter);
		
		ObjMaskProviderExtractText provider = new ObjMaskProviderExtractText();
		provider.init( ImageInitParams.create(
			logger,
			new RandomNumberGeneratorMersenneConstant(),
			modelDir
		), logger);
		return provider;
	}
}
