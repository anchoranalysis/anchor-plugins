package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import static org.junit.Assert.*;

import java.util.Optional;

import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class ObjMaskSgmnWatershedYeongTest {

	private final String PATH_CHNL_BLURRED = "chnlInBlurred.tif";
	private final String PATH_EXPECTED_NO_MASKS_NO_SEEDS = "blurredResult_noMasks_noSeeds.h5";
	
	private TestLoaderImageIO loader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir("watershed01/")
	);
	
	@Test
	public void test() throws SgmnFailedException, TestDataLoadException, OutputWriteFailedException {
		sgmn(PATH_EXPECTED_NO_MASKS_NO_SEEDS, Optional.empty());
	}
	
	private void sgmn( String pathObjsExpected, Optional<String> pathMask ) throws SgmnFailedException, TestDataLoadException {
		ObjMaskSgmnWatershedYeong sgmn = new ObjMaskSgmnWatershedYeong();
		
		ObjMaskCollection objsResult = sgmn.sgmn(
			chnlInput(),
			Optional.empty()
		);
		
		ObjMaskCollection objsExpected = loader.openObjsFromTestPath(pathObjsExpected);
		
		assertTrue( objsExpected.equalsDeep(objsResult) );
	}
	
	private Chnl chnlInput() {
		return loader.openStackFromTestPath(PATH_CHNL_BLURRED).getChnl(0);
	}
}
