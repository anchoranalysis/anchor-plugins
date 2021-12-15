package org.anchoranalysis.plugin.opencv.resizer;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.datatype.FloatVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.test.image.ChannelFixture;
import org.junit.jupiter.api.Test;

/**
 * Likes {@link OpenCV}.
 *
 * @author Owen Feehan
 */
class OpenCVTest {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    /** The resizer to test. */
    private VoxelsResizer resizer = new VoxelsResizerOpenCV();

    @Test
    void testDownscale() throws OperationFailedException {
        CVInit.blockUntilLoaded();
        doTest(ChannelFixture.LARGE_2D, ChannelFixture.MEDIUM_2D);
    }

    /**
     * Creates {@link Voxels}, performs resizing, and checks that certain statistics are similar
     * afterwards.
     *
     * <p>The {@link Voxels} are created for three different data-types:
     *
     * <ul>
     *   <li>unsigned byte
     *   <li>unsigned short
     *   <li>float
     * </ul>
     *
     * @param sizeBefore the size of {@link Voxels} to create, <b>before</b> resizing.
     * @param sizeAfter the size of {@link Voxels} to create, <b>after</b> resizing.
     */
    private void doTest(Extent sizeBefore, Extent sizeAfter) throws OperationFailedException {
        doTestWithType(sizeBefore, sizeAfter, UnsignedByteVoxelType.INSTANCE);
        doTestWithType(sizeBefore, sizeAfter, UnsignedShortVoxelType.INSTANCE);
        doTestWithType(sizeBefore, sizeAfter, FloatVoxelType.INSTANCE);
    }

    private void doTestWithType(Extent sizeBefore, Extent sizeAfter, VoxelDataType voxelDataType)
            throws OperationFailedException {
        Channel channel =
                new ChannelFixture()
                        .createChannel(sizeBefore, ChannelFixture::diffModulo, voxelDataType);

        VoxelsUntyped voxelsResized =
                VoxelsFactory.instance().createEmpty(sizeAfter, voxelDataType);

        resizer.resize(channel.voxels(), voxelsResized);

        CompareHistogramStatistics.assertSimilar(
                channel.voxels(), voxelsResized, !resizer.canValueRangeChange());
    }
}
