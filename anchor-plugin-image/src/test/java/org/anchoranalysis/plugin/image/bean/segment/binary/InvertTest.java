package org.anchoranalysis.plugin.image.bean.segment.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.anchoranalysis.image.bean.nonbean.segment.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxelsFactory;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.spatial.box.Extent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link Invert} class. */
class InvertTest {

    /** The {@link Invert} instance to be tested. */
    private Invert invert;

    /** Sets up the test environment before each test method. */
    @BeforeEach
    void setUp() {
        invert = new Invert();
    }

    /**
     * Tests the inversion of a 2D binary image.
     *
     * @throws SegmentationFailedException if the segmentation fails.
     */
    @Test
    void testInvert2DImage() throws SegmentationFailedException {
        BinaryVoxels<UnsignedByteBuffer> inputVoxels = createInputVoxels();
        BinarySegmentation mockSegmentation = createMockSegmentation(inputVoxels);
        BinaryVoxels<UnsignedByteBuffer> result = performInversion(mockSegmentation);
        assertInvertedResult(result);
    }

    /**
     * Creates the input voxels for testing.
     *
     * @return a {@link BinaryVoxels} object with test data.
     */
    private BinaryVoxels<UnsignedByteBuffer> createInputVoxels() {
        BinaryVoxels<UnsignedByteBuffer> inputVoxels =
                BinaryVoxelsFactory.createEmptyOff(new Extent(3, 3, 1));
        UnsignedByteBuffer buffer = inputVoxels.sliceBuffer(0);
        fillInputBuffer(buffer);
        return inputVoxels;
    }

    /**
     * Fills the input buffer with test data.
     *
     * @param buffer the {@link UnsignedByteBuffer} to fill.
     */
    private void fillInputBuffer(UnsignedByteBuffer buffer) {
        byte on = BinaryValuesByte.getDefault().getOn();
        byte off = BinaryValuesByte.getDefault().getOff();
        byte[] values = {on, off, on, off, on, off, on, off, on};
        for (int i = 0; i < values.length; i++) {
            buffer.putRaw(i, values[i]);
        }
    }

    /**
     * Creates a mock {@link BinarySegmentation} that returns the given input voxels.
     *
     * @param inputVoxels the {@link BinaryVoxels} to be returned by the mock.
     * @return a mock {@link BinarySegmentation}.
     * @throws SegmentationFailedException if the segmentation fails.
     */
    private BinarySegmentation createMockSegmentation(BinaryVoxels<UnsignedByteBuffer> inputVoxels)
            throws SegmentationFailedException {
        BinarySegmentation mockSegmentation = mock(BinarySegmentation.class);
        when(mockSegmentation.segment(any(), any(), any())).thenReturn(inputVoxels);
        return mockSegmentation;
    }

    /**
     * Performs the inversion operation using the {@link Invert} instance.
     *
     * @param mockSegmentation the mock {@link BinarySegmentation} to use.
     * @return the inverted {@link BinaryVoxels}.
     * @throws SegmentationFailedException if the segmentation fails.
     */
    private BinaryVoxels<UnsignedByteBuffer> performInversion(BinarySegmentation mockSegmentation)
            throws SegmentationFailedException {
        return invert.segmentFromExistingSegmentation(
                mock(VoxelsUntyped.class),
                mock(BinarySegmentationParameters.class),
                Optional.empty(),
                mockSegmentation);
    }

    /**
     * Asserts that the result of the inversion is correct.
     *
     * @param result the {@link BinaryVoxels} result to check.
     */
    private void assertInvertedResult(BinaryVoxels<UnsignedByteBuffer> result) {
        UnsignedByteBuffer resultBuffer = result.sliceBuffer(0);
        byte on = BinaryValuesByte.getDefault().getOn();
        byte off = BinaryValuesByte.getDefault().getOff();
        byte[] expectedValues = {off, on, off, on, off, on, off, on, off};
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], resultBuffer.getRaw(i));
        }
    }
}
