package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import static org.junit.Assert.*;

import java.util.Optional;

import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class ObjMaskSgmnWatershedYeongTest {

	private final static String PATH_CHNL_BLURRED = "chnlInBlurred.tif";
	private final static String PATH_MASK = "mask.tif";
	
	private final static String PATH_EXPECTED_NO_MASKS_NO_SEEDS = "blurredResult_noMasks_noSeeds.h5";
	private final static String PATH_EXPECTED_MASKS_NO_SEEDS = "blurredResult_masks_noSeeds.h5";
	
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
		
		Optional<ObjectMask> mask = pathMask.map( path ->
			mask(path)
		); 
		
		ObjectCollection objsResult = sgmn.sgmn(
			chnl(PATH_CHNL_BLURRED),
			mask,
			Optional.empty()
		);	
					
		ObjectCollection objsExpected = loader.openObjsFromTestPath(pathObjsExpected);
		
		assertTrue( objsExpected.equalsDeep(objsResult) );
	}
	
	private ObjectMask mask(String path) {
		BinaryChnl chnl = new BinaryChnl(
			chnl(PATH_MASK),
			BinaryValues.getDefault()
		);
		return new ObjectMask(chnl.binaryVoxelBox());
	}
	
	private Channel chnl(String path) {
		return loader.openChnlFromTestPath(path);
	}
}
