package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.OperationFailedRuntimeException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.DualComparer;

class CompareHelper {

	public static void compareOutputWithSaved( Path pathAbsoluteOutput, String pathRelativeSaved, String[] relativePaths ) throws OperationFailedException {
		TestLoader loaderTempDir = TestLoader.createFromExplicitDirectory(pathAbsoluteOutput);
		TestLoader loaderSavedObjs = TestLoader.createFromMavenWorkingDir(pathRelativeSaved);
		
		DualComparer comparer = new DualComparer(
			loaderTempDir,
			loaderSavedObjs
		);
		
		try {
			for(String path : relativePaths) {
				assertIdentical(comparer, path);	
			}
		} catch (IOException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static void assertIdentical(DualComparer comparer, String relativePath) throws IOException {
		assertTrue(
			relativePath + " is not identical",
			compareForExtr(comparer, relativePath)
		);
	}
	
	private static boolean compareForExtr(DualComparer comparer, String relativePath) throws IOException {
		if (hasExtension(relativePath,".tif")) {
			return comparer.compareTwoImages(relativePath);
		} else if (hasExtension(relativePath,".csv")) {
			return comparer.compareTwoCsvFiles( relativePath, ",", true, 0, true, false, System.out);
		} else if (hasExtension(relativePath,".xml")) {
			return comparer.compareTwoXmlDocuments(relativePath);
		} else {
			throw new OperationFailedRuntimeException("Extension not supported");
		}
	}
	
	/** Does a string end in an extension, ignoring case? */
	private static boolean hasExtension( String str, String endsWith ) {
		return str.toLowerCase().endsWith(endsWith);
	}
	
}
