package org.anchoranalysis.plugin.image.task.bean.format;

import java.nio.file.Path;
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
                new String[] {"converted.tif"});
    }

    @Test
    void testIndependent3() throws OperationFailedException {
        performTest(
                new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 3, false),
                "independent3",
                new String[] {"channel00.tif", "channel01.tif", "channel02.tif"});
    }

    @Test
    void testRGB3() throws OperationFailedException {
        performTest(
                new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 3, true),
                "rgb",
                new String[] {"converted.tif"});
    }

    private void performTest(
            ChannelSpecification channelSpecification,
            String resourceDirectorySuffix,
            String[] filenamesToCompare)
            throws OperationFailedException {

        ConvertImageFormat task = new ConvertImageFormat();

        NamedChannelsInputFixture input = new NamedChannelsInputFixture(channelSpecification);

        TaskSingleInputHelper.runTaskAndCompareOutputs(
                input, task, directory, "convert/" + resourceDirectorySuffix, filenamesToCompare);
    }
}
