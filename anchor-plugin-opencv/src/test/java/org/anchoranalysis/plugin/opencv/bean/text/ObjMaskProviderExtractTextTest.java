package org.anchoranalysis.plugin.opencv.bean.text;



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
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.stack.StackProviderHolder;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.BoundContextFixture;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * Tests ObjMaskProviderExtractTest
 * 
 * <p>Note that the weights file for the EAST model is duplicated in src/test/resources, as well as its usual location
 * in the models/ directory of the Anchor distribution. This is as it's difficult to determine a path to the models/
 * directory at test-time</p>
 * 
 * @author owen
 *
 */
public class ObjMaskProviderExtractTextTest {

	private TestLoaderImageIO testLoader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	@Test
	public void testCar() throws AnchorIOException, CreateException, InitException {
		
		ObjMaskProviderExtractText provider = createAndInitProvider("car.jpg");
				
		ObjectCollection objs = provider.create();
		
		assertTrue( objs.size()==3 );
		
		assertBoxAtIndex(objs, 0, boxAt(439, 311, 75, 37) );
		assertBoxAtIndex(objs, 1, boxAt(310, 318, 108, 36) );
		assertBoxAtIndex(objs, 2, boxAt(392, 199, 29, 26) );
	}
	
	private void assertBoxAtIndex( ObjectCollection objs, int index, BoundingBox box  ) {
		assertEquals(
			"box at index " + index,
			box,
			objs.get(index).getBoundingBox()
		);		
	}
	
	private ObjMaskProviderExtractText createAndInitProvider( String imageFilename ) throws InitException {
		
		ObjMaskProviderExtractText provider = new ObjMaskProviderExtractText();
		
		Stack stack = testLoader.openStackFromTestPath(imageFilename);
		provider.setStackProvider( new StackProviderHolder(stack) );
		
		initProvider(
			provider,
			BoundContextFixture.withSimpleLogger(
				testLoader.getTestLoader().getRoot()
			)
		);
		
		return provider;
	}
	
	private static void initProvider( ObjMaskProviderExtractText provider, BoundIOContext context ) throws InitException {
		provider.init(
			ImageInitParamsFactory.create(context),
			context.getLogger()
		);		
	}
	
	/** Bounding box at particular point and coordinates */
	private static BoundingBox boxAt( int x, int y, int width, int height ) {
		return new BoundingBox(
			new Point3i(x, y, 0),
			new Extent(width, height, 1)
		);
	}
}
