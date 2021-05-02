package org.anchoranalysis.plugin.image.task.bean.format;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.TaskSingleInputHelper;
import org.anchoranalysis.test.image.rasterwriter.ChannelSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link ConvertImageFormat}.
 *
 * @author Owen Feehan
 */
class ConvertImageFormatTest {

    private static final List<String> FILENAME_CONVERTED = Arrays.asList("converted.tif");

    private static final List<String> FILENAMES_INDEPENDENT =
            Arrays.asList("channel00.tif", "channel01.tif", "channel02.tif");

    @TempDir Path directory;

    @BeforeAll
    static void setup() {
        RegisterBeanFactories.registerAllPackageBeanFactories();
    }

    @Test
    void testSingle() throws OperationFailedException {
        performTest(
                new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 1, false),
                "single",
                FILENAME_CONVERTED);
    }

    @Test
    void testIndependent3() throws OperationFailedException {
        performTest(
                new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 3, false),
                "independent3",
                FILENAMES_INDEPENDENT);
    }

    @Test
    void testRGB3() throws OperationFailedException {
        performTest(
                new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 3, true),
                "rgb",
                FILENAME_CONVERTED);
    }

    private void performTest(
            ChannelSpecification channelSpecification,
            String resourceDirectorySuffix,
            Iterable<String> filenamesToCompare)
            throws OperationFailedException {

        ConvertImageFormat task = new ConvertImageFormat();

        NamedChannelsInputFixture input = new NamedChannelsInputFixture(channelSpecification);

        TaskSingleInputHelper.runTaskAndCompareOutputs(
                input, task, directory, "convert/" + resourceDirectorySuffix, filenamesToCompare);
    }
}
