/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
