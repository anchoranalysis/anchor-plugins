package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.Optional;

import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.io.objs.GeneratorHDF5;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class ObjMaskSgmnWatershedYeongTest {

	private final String PATH_CHNL_BLURRED = "chnlInBlurred.tif";
	private final String PATH_MASK = "mask.tif";
	
	private final String PATH_EXPECTED_NO_MASKS_NO_SEEDS = "blurredResult_noMasks_noSeeds.h5";
	private final String PATH_EXPECTED_MASKS_NO_SEEDS = "blurredResult_masks_noSeeds.h5";
	
	private TestLoaderImageIO loader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir("watershed01/")
	);
	
	@Test
	public void test_noMasks_noSeeds() throws SgmnFailedException, TestDataLoadException, OutputWriteFailedException {
		sgmn(
			PATH_EXPECTED_NO_MASKS_NO_SEEDS,
			Optional.empty()
		);
	}
	
	@Test
	public void test_masks_noSeeds() throws SgmnFailedException, TestDataLoadException, OutputWriteFailedException {
		sgmn(
			PATH_EXPECTED_MASKS_NO_SEEDS,
			Optional.of(PATH_MASK)
		);
	}
	
	private void sgmn( String pathObjsExpected, Optional<String> pathMask ) throws SgmnFailedException, TestDataLoadException, OutputWriteFailedException {
		ObjMaskSgmnWatershedYeong sgmn = new ObjMaskSgmnWatershedYeong();
		
		ObjMaskCollection objsResult = pathMask.isPresent() ?
				sgmn.sgmn(
					chnl(PATH_CHNL_BLURRED),
					mask(pathMask.get()),
					Optional.empty()
				)	
		: sgmn.sgmn(
			chnl(PATH_CHNL_BLURRED),
			Optional.empty()
		);
							
		//GeneratorHDF5.writeObjsToFile(objsResult, Paths.get("C:\\Users\\owen\\code\\anchor\\anchor-plugins\\anchor-plugin-image\\src\\test\\resources\\watershed01\\result.h5"));
					
		ObjMaskCollection objsExpected = loader.openObjsFromTestPath(pathObjsExpected);
		
		assertTrue( objsExpected.equalsDeep(objsResult) );
	}
	
	private ObjMask mask(String path) {
		BinaryChnl chnl = new BinaryChnl(
			chnl(PATH_MASK),
			BinaryValues.getDefault()
		);
		return new ObjMask(chnl.binaryVoxelBox());
	}
	
	private Chnl chnl(String path) {
		return loader.openChnlFromTestPath(path);
	}
}
