package ch.ethz.biol.cell.imageprocessing.io.filepathgenerator;

/*
 * #%L
 * anchor-plugin-eth
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorRegEx;
import org.anchoranalysis.io.error.AnchorIOException;

public class NucleiModelFilePathGenerator extends FilePathGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	// END BEAN PROPERTIES
	
	@Override
	public Path outFilePath(Path pathIn, boolean debugMode) throws AnchorIOException {
		
		try {
			String regEx = "experiments/nuclei/3D/filteredImage/(.*)_1.0/(.*)/stack_nucleiScaled.tif";
			
			Matcher m = FilePathGeneratorRegEx.match(pathIn, regEx);
			
			if (m.groupCount()!=2) {
				throw new AnchorIOException(String.format("Cannot match two groups from %s",pathIn));
			}
			
			Path set = Paths.get(m.group(2));
			String groupName = groupNameFromSet( set );
			
			//outPath="experiments/nuclei/3D/extractParamsModel/$1_1.0/$2/$3/paramsGroupAgg.xml"/>
			
			StringBuilder sb = new StringBuilder();
			sb.append("experiments/nuclei/3D/extractParamsModel/");
			sb.append( m.group(1) );
			sb.append("_1.0/");
			sb.append( groupName );
			sb.append( "/paramsGroupAgg.xml" );
			return Paths.get(sb.toString());
			
		} catch (OperationFailedException e) {
			throw new AnchorIOException("An error occurred matching the regular-expression", e);
		}
	}
	
	
	private static String groupNameFromSet( Path set ) throws OperationFailedException {
		String groupNameFirst = groupNameFromSetSingleFolder(set);
		
		if (groupNameFirst.equals("blackrussian") || groupNameFirst.equals("whiterussian") || groupNameFirst.equals("screwdriver")) {
			String groupNameSecond = groupNameFromSetTwoFolder(set);
			return(groupNameSecond);
		}
		
	//if (groupNameFirst=='cubalibre' || groupNameFirst=='zombie' || groupNameFirst=='swimmingpool') {
	//		return(set);
	//	}
		
		return(groupNameFirst);
	}
	
	
	// Chromosomes
//	private static String groupNameFromSet( String set ) throws OperationFailedException {
//		
//		String groupNameFirst = groupNameFromSetSingleFolder(set);
//		
//		if (groupNameFirst.equals("ginfizz")) {
//			return(groupNameFirst);
//		} else {
//			
//			String groupNameSecond = groupNameFromSetTwoFolder(set);
//			
//			if (groupNameSecond.equals("royrogers/weakest aneuploidy NS NS (control) (plus VHL)")) {
//				
//				String groupNameThird = groupNameFromSetThreeFolder(set);
//				
//				if (groupNameThird.equals("royrogers/weakest aneuploidy NS NS (control) (plus VHL)/NS NS 25")) {
//					return(groupNameThird);
//				} else {
//					return(groupNameSecond);
//				}
//			}
//			
//			return(groupNameSecond);
//		}
//	}
	
	
	private static String groupNameFromSetSingleFolder( Path set ) throws OperationFailedException {
		return FilePathGeneratorRegEx.match(set, "([^/]+)/.*").group(1);
	}

	private static String groupNameFromSetTwoFolder( Path set ) throws OperationFailedException {
		Matcher m = FilePathGeneratorRegEx.match(set, "([^/]+)/([^/]+)/.*");
		return m.group(1) + "/" + m.group(2);
	}

	@SuppressWarnings("unused")
	private static String groupNameFromSetThreeFolder( Path set ) throws OperationFailedException {
		Matcher m = FilePathGeneratorRegEx.match(set, "([^/]+)/([^/]+)/([^/]+)/.*");
		return m.group(1) + "/" + m.group(2) + m.group(3);
	}
}
