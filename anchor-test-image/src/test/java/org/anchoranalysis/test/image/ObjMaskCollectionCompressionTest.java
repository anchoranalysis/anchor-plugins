package org.anchoranalysis.test.image;

import static org.anchoranalysis.test.image.HelperReadWriteObjs.*;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import anchor.test.TestLoader;
import anchor.test.image.TestLoaderImageIO;

public class ObjMaskCollectionCompressionTest {
		
	// An uncompressed obj-mask-collection
	private static final String PATH_UNCOMPRESSED_OBJS = "objsUncompressed/objs.h5";
	
	private TestLoaderImageIO testLoader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testCompression() throws SetOperationFailedException, DeserializationFailedException {
		
		ObjMaskCollectionWithSize uncompressed = calcUncompressed(PATH_UNCOMPRESSED_OBJS);
		
		ObjMaskCollectionWithSize compressed = calcCompressed(
			uncompressed.getObjs(),
			folder.getRoot().toPath()
		);

		double relativeSize = uncompressed.relativeSize(compressed);
		
		assertTrue( uncompressed.getObjs().equalsDeep(compressed.getObjs()) );

		// We expect compression of approximate 6.26 on this particular example
		assertTrue( relativeSize > 6.1 && relativeSize < 6.3 );
	}
	
	private ObjMaskCollectionWithSize calcUncompressed(String pathIn) {
				
		// Read the object, and write it again, this time compressed
		ObjMaskCollection objs = testLoader.openObjsFromTestPath(pathIn);
		
		long size = fileSizeBytes(
			testLoader.getTestLoader().resolveTestPath(pathIn)
		);
		
		return new ObjMaskCollectionWithSize(objs, size);
	}

	
	private static ObjMaskCollectionWithSize calcCompressed( ObjMaskCollection objsUncompressed, Path root ) throws SetOperationFailedException, DeserializationFailedException {
		
		Path pathOut = root.resolve(TEMPORARY_FOLDER_OUT+".h5");
		
		ObjMaskCollection objsCompressed = writeAndReadAgain( objsUncompressed, root, pathOut );
				
		long size = fileSizeBytes( pathOut );
		
		return new ObjMaskCollectionWithSize( objsCompressed, size );
	}
	
	private static ObjMaskCollection writeAndReadAgain( ObjMaskCollection objs, Path pathRoot, Path pathOut ) throws SetOperationFailedException, DeserializationFailedException {
		// Write the objs to the file-system and read again
		writeObjs(objs, pathRoot, generator(true,true) );
		return readObjs(pathOut);		
	}
	
	private static long fileSizeBytes( Path testPath ) {
		return testPath.toFile().length();
	}
}
