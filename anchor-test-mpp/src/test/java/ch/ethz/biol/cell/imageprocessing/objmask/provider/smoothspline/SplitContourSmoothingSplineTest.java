package ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline;

/*-
 * #%L
 * anchor-test-mpp
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.anchoranalysis.test.mpp.LoadUtilities;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SplitContourSmoothingSplineTest {

		
	private TestLoaderImageIO testLoader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	@Test
	public void test() throws CreateException, OperationFailedException, SetOperationFailedException {
		
		ObjMask contourIn = LoadUtilities.openLargestObjectBinaryFrom("01", testLoader);
		
		ContourList contours = SplitContourSmoothingSpline.apply(contourIn, 0.001, 0, 30);
		
		assertTrue( contours.size()==72 );
		
		// DEBUG CODE - a dependency needs also be to be added to anchor-io-generator
		/*DisplayStack background = DisplayStack.create(stack, new ChnlFactoryMulti());
		TempBoundOutputManager.instance().getBoundOutputManager().getWriterAlwaysAllowed().write(
			"contours",
			new ContourListGenerator( background, contours )
		);*/
	}
}
