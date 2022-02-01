/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedBiConsumer;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.image.bean.nonbean.ConsistentChannelChecker;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;

/**
 * Exports a histogram of voxel intensities as a CSV file for each channel of an image.
 *
 * <p>Additionally, a histogram with the summation of voxel intensities for all channels in each
 * image is produced.
 *
 * <p>Optionally, one channel can be used as a mask, to restrict which voxels are included in the
 * histogram.
 *
 * <p>These steps occur:
 *
 * <ol>
 *   <li>All files are aggregated into groups.
 *   <li>For each image file, a histogram is calculated.
 *   <li>The histogram is added to the group histogram.
 *   <li>The histograms are written to the filesystem.
 * </ol>
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@value ExportImageHistograms#OUTPUT_HISTOGRAMS}</td><td>yes</td><td>A separate CSV histogram for each channel's voxels intensity.</td></tr>
 * <tr><td>{@value ExportImageHistograms#OUTPUT_SUM}</td><td>yes</td><td>A histogram for the sum of each voxel's intensity across all channels.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 */
public class ExportImageHistograms extends GroupedStackBase<Histogram, Histogram> {

    /**
     * The histograms written out for each channel (further filtered by a second-level check on the
     * channel name).
     *
     * <p>Note that the output-name does not feature in the filenames of outputted histograms.
     */
    private static final String OUTPUT_HISTOGRAMS = "channels";

    /** Summed CSV histograms of the selected channels in a subdirectory. */
    private static final String OUTPUT_SUM = "sum";

    // START BEAN PROPERTIES
    /**
     * If defined, this is the name of channel used as a mask over the values which are fed into the
     * histogram
     */
    @BeanField @AllowEmpty @Getter @Setter private String channelMask = "";

    /** What voxel value to read as "On" in the mask above. */
    @BeanField @Getter @Setter private int maskValue = 255;

    /** Iff true, bins with zero-counts are not written as a row in the CSV file. */
    @BeanField @Getter @Setter private boolean csvIgnoreZeros = false;
    // END BEAN PROPERTIES

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_HISTOGRAMS, OUTPUT_SUM);
    }

    @Override
    protected GroupMapByName<Histogram, Histogram> createGroupMap(
            ConsistentChannelChecker channelChecker,
            Stream<Optional<String>> groupIdentifiers,
            Optional<InputOutputContext> outputContext,
            OperationContext operationContext) {
        int maxIntensityValue = (int) channelChecker.getVoxelDataType().maxValue();
        return new GroupedHistogramMap(
                createWriter(), groupIdentifiers, outputContext, maxIntensityValue);
    }

    @Override
    protected Optional<String> subdirectoryForGroupOutputs() {
        return Optional.of(OUTPUT_SUM);
    }

    @Override
    protected CheckedFunction<Channel, Histogram, CreateException> createChannelDeriver(
            ChannelSource source) throws OperationFailedException {
        Optional<Mask> mask = MaskExtracter.extractMask(source, channelMask, maskValue);
        return channel -> HistogramFromObjectsFactory.createFrom(channel, mask);
    }

    @Override
    protected void processIndividual(
            String name,
            Histogram individual,
            CheckedBiConsumer<String, Histogram, OperationFailedException> consumeIndividual,
            InputOutputContext context)
            throws OperationFailedException {

        createWriter().writeHistogramToFile(individual, name, context);

        consumeIndividual.accept(name, individual);
    }

    @Override
    protected String outputNameForGroups() {
        return OUTPUT_SUM;
    }

    private GroupedHistogramWriter createWriter() {
        return new GroupedHistogramWriter(OUTPUT_HISTOGRAMS, csvIgnoreZeros);
    }
}
