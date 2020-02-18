package org.anchoranalysis.test.image.contiguouspath;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.outline.traverser.contiguouspath.ContiguousPixelPath;

class ContiguousPixelPathReader {

    // Create a Pattern object
    private static Pattern pattern = Pattern.compile("\\[(\\d+),(\\d+),(\\d+)\\]");

    /**
     * Reads a ContiguousPixelPath from a text-file with particular formatting
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public static ContiguousPixelPath readFromFile(Path path) throws IOException {
    	List<Point3i> pts = pointsFromFile(path);
    	
    	ContiguousPixelPath out = new ContiguousPixelPath();
    	out.insertAfter(pts);
    	return out;
    }
    
	private static List<Point3i> pointsFromFile(Path path) throws IOException {
		List<String> strs = pointStringsFromFile(path);
		
		List<Point3i> out = new ArrayList<>();
		for( String s : strs) {
			out.add( convertStr(s) );
		}
		
		return out;
	}
	
	private static Point3i convertStr( String str ) {

      // Now create matcher object.
	    Matcher m = pattern.matcher(str);
	    
	    assert( m.matches() );
	    assert( m.groupCount()==3 );
	    
	    return new Point3i(
	    	convertInt( m.group(1) ),
	    	convertInt( m.group(2) ),
	    	convertInt( m.group(3) )
	    );
	}
	
	private static int convertInt( String str ) {
		return Integer.parseInt(str);
	}
	
	private static List<String> pointStringsFromFile(Path path) throws IOException {

		List<String> out = new ArrayList<String>();
		
		List<String> lines = Files.readAllLines(path);
		for( String line : lines ) {
			extractPartsFromLine(line, out);
		}
		
		return out;
	}
	
	private static void extractPartsFromLine( String line, List<String> out ) {
		String[] parts = line.split(", ");
		for( String s : parts) {
			s = s.replaceAll("\\s+","");
			if (!s.isEmpty()) {
				out.add(s);
			}
		}		
	}
	
}
