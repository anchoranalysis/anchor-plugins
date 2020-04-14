package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.anchoranalysis.core.error.OperationFailedRuntimeException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.DualComparer;

class CompareHelper {

	public static void compareOutputWithSaved( Path pathAbsoluteOutput, String pathRelativeSaved, String[] relativePaths ) throws IOException {
		TestLoader loaderTempDir = TestLoader.createFromExplicitDirectory(pathAbsoluteOutput);
		TestLoader loaderSavedObjs = TestLoader.createFromMavenWorkingDir(pathRelativeSaved);
		
		DualComparer comparer = new DualComparer(
			loaderTempDir,
			loaderSavedObjs
		);
				
		for(String path : relativePaths) {
			assertIdentical(comparer, path);	
		}
	}
	
	private static void assertIdentical(DualComparer comparer, String relativePath) throws IOException {
		assertTrue(
			relativePath,
			compareForExtr(comparer, relativePath)
		);
	}
	
	private static boolean compareForExtr(DualComparer comparer, String relativePath) throws IOException {
		if (hasExtension(relativePath,".tif")) {
			return comparer.compareTwoImages(relativePath);
		} else if (hasExtension(relativePath,".csv")) {
			return comparer.compareTwoCsvFiles( relativePath, ",", true, 0, true, false, System.out);
		} else {
			throw new OperationFailedRuntimeException("Extension not supported");
		}
	}
	
	/** Does a string end in an extension, ignoring case? */
	private static boolean hasExtension( String str, String endsWith ) {
		return str.toLowerCase().endsWith(endsWith);
	}
	
}
