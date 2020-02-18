package org.anchoranalysis.image.outline.traverser.visitedpixels.combine;

/*-
 * #%L
 * anchor-test-image
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.outline.traverser.contiguouspath.ContiguousPixelPath;
import org.anchoranalysis.image.outline.traverser.contiguouspath.PointsListNghbUtilities;
import org.anchoranalysis.image.outline.traverser.visitedpixels.combine.mergestrategy.MergeCandidate;
import org.junit.Test;

import anchor.test.TestLoader;

public class CombineContiguousPixelPathsTest {

	private TestLoader loader = TestLoader.createFromMavenWorkingDirTest();
	
	@Test
	public void test01() throws IOException {
		doTestForNum("01", new Point3i(548,159,0));
	}
	
	@Test
	public void test02() throws IOException {
		doTestForNum("02", new Point3i(507,277,0));
	}
	
	private void doTestForNum( String prefix, Point3i mergePnt ) throws IOException {
		ContiguousPixelPath toKeep = readTestData( prefix + "/toKeep");
		ContiguousPixelPath toMerge = readTestData( prefix + "/toMerge");
		
		new MergeCandidate(toKeep, toMerge, mergePnt, mergePnt).merge();
		
		
		assertTrue( PointsListNghbUtilities.areNghbDistinct(toKeep.points()) );
		assertTrue( PointsListNghbUtilities.areAllPointsInBigNghb(toKeep.points()) );		
	}
	
	private ContiguousPixelPath readTestData( String id ) throws IOException {
		Path pathAbs = loader.resolveTestPath("contiguousPixelPaths/" + id + ".csv");
		return ContiguousPixelPathReader.readFromFile(pathAbs);
	}
}
