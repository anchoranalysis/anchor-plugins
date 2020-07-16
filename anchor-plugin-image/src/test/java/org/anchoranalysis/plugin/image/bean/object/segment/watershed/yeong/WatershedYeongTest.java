/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import static org.junit.Assert.*;

import java.util.Optional;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class WatershedYeongTest {

    private static final String PATH_CHNL_BLURRED = "chnlInBlurred.tif";
    private static final String PATH_MASK = "mask.tif";

    private static final String PATH_EXPECTED_NO_MASKS_NO_SEEDS =
            "blurredResult_noMasks_noSeeds.h5";
    private static final String PATH_EXPECTED_MASKS_NO_SEEDS = "blurredResult_masks_noSeeds.h5";

    private TestLoaderImageIO loader =
            new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory("watershed01/"));

    @Test
    public void test_noMasks_noSeeds()
            throws SegmentationFailedException, TestDataLoadException, OutputWriteFailedException {
        sgmn(PATH_EXPECTED_NO_MASKS_NO_SEEDS, Optional.empty());
    }

    @Test
    public void test_masks_noSeeds()
            throws SegmentationFailedException, TestDataLoadException, OutputWriteFailedException {
        sgmn(PATH_EXPECTED_MASKS_NO_SEEDS, Optional.of(PATH_MASK));
    }

    private void sgmn(String pathObjectsExpected, Optional<String> pathMask)
            throws SegmentationFailedException, TestDataLoadException, OutputWriteFailedException {
        WatershedYeong sgmn = new WatershedYeong();

        Optional<ObjectMask> mask = pathMask.map(path -> mask(path));

        ObjectCollection objectsResult =
                sgmn.segment(chnl(PATH_CHNL_BLURRED), mask, Optional.empty());

        ObjectCollection objectsExpected = loader.openObjectsFromTestPath(pathObjectsExpected);

        assertTrue(objectsExpected.equalsDeep(objectsResult));
    }

    private ObjectMask mask(String path) {
        Mask chnl = new Mask(chnl(PATH_MASK), BinaryValues.getDefault());
        return new ObjectMask(chnl.binaryVoxelBox());
    }

    private Channel chnl(String path) {
        return loader.openChnlFromTestPath(path);
    }
}
