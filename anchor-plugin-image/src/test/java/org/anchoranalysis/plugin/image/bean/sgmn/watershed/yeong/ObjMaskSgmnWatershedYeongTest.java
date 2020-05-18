package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import static org.junit.Assert.*;

import java.util.Optional;

import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class ObjMaskSgmnWatershedYeongTest {

	private TestLoaderImageIO loader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	@Test
	public void test() throws SgmnFailedException, TestDataLoadException, OutputWriteFailedException {
		
		ObjMaskSgmnWatershedYeong sgmn = new ObjMaskSgmnWatershedYeong();
		ObjMaskCollection objs = sgmn.sgmn(
			loader.openStackFromTestPath("watershed01/chnlInBlurred.tif").getChnl(0),
			Optional.empty()
		);
		
		ObjMaskCollection objsExpected = loader.openObjsFromTestPath("watershed01/blurredResult_noMasks_noSeeds.h5");
		
		assertTrue( objsExpected.equalsDeep(objs) );
	}

}
