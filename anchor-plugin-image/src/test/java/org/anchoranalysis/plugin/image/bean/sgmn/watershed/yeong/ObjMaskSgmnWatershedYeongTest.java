package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;

import org.anchoranalysis.image.bean.nonbean.error.SgmnFailedException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class ObjMaskSgmnWatershedYeongTest {

	private static final String PATH_CHNL_BLURRED = "chnlInBlurred.tif";
	private static final String PATH_MASK = "mask.tif";
	
	private static final String PATH_EXPECTED_NO_MASKS_NO_SEEDS = "blurredResult_noMasks_noSeeds.h5";
	private static final String PATH_EXPECTED_MASKS_NO_SEEDS = "blurredResult_masks_noSeeds.h5";
	
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
