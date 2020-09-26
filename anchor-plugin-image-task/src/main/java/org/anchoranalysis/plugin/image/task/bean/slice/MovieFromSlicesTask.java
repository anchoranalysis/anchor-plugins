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

package org.anchoranalysis.plugin.image.task.bean.slice;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncremental;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalRerouterErrors;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.namestyle.StringSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

public class MovieFromSlicesTask extends RasterTask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int delaySizeAtEnd = 0;

    @BeanField @Getter @Setter private String filePrefix = "movie";

    @BeanField @OptionalBean @Getter @Setter private SizeXY size;

    @BeanField @Getter @Setter private int startIndex = 0; // this does nothing atm

    @BeanField @Getter @Setter private int repeat = 1;
    // END BEAN PROPERTIES

    private int index = 0;
    private GeneratorSequenceNonIncrementalRerouterErrors<Stack> generatorSeq;

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {

        StackGenerator generator = new StackGenerator(false, "out", false);

        generatorSeq =
                new GeneratorSequenceNonIncrementalRerouterErrors<>(
                        new GeneratorSequenceNonIncremental<>(
                                outputManager.getDelegate(),
                                Optional.empty(),
                                // NOTE WE ARE NOT ASSIGNING A NAME TO THE OUTPUT
                                new StringSuffixOutputNameStyle("", "%s"),
                                generator,
                                true),
                        errorReporter);

        // TODO it would be nicer to reflect the real sequence type, than just using a set of
        // indexes
        generatorSeq.start(new SetSequenceType());
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    private String formatIndex(int index) {
        return String.format("%s_%08d", filePrefix, index);
    }

    @Override
    public void doStack(
            NamedChannelsInput inputObject, int seriesIndex, int numSeries, BoundIOContext context)
            throws JobExecutionException {

        try {
            NamedChannelsForSeries ncc =
                    inputObject.createChannelsForSeries(0, ProgressReporterNull.get());

            ProgressReporter progressReporter = ProgressReporterNull.get();

            Channel red = ncc.getChannel("red", 0, progressReporter);
            Channel blue = ncc.getChannel("blue", 0, progressReporter);
            Channel green = ncc.getChannel("green", 0, progressReporter);

            //
            if (!red.dimensions().equals(blue.dimensions())
                    || !blue.dimensions().equals(green.dimensions())) {
                throw new JobExecutionException("Scene dimensions do not match");
            }

            Stack sliceOut = null;

            ExtractProjectedStack extract =
                    new ExtractProjectedStack(Optional.ofNullable(size).map(SizeXY::asExtent));

            for (int z = 0; z < red.dimensions().z(); z++) {

                sliceOut = extract.extractAndProjectStack(red, green, blue, z);

                for (int i = 0; i < repeat; i++) {
                    generatorSeq.add(sliceOut, formatIndex(index));
                    index++;
                }
            }

            // Just
            if (delaySizeAtEnd > 0 && sliceOut != null) {
                for (int i = 0; i < delaySizeAtEnd; i++) {
                    generatorSeq.add(sliceOut, formatIndex(index));
                    index++;
                }
            }

        } catch (RasterIOException | IncorrectImageSizeException | GetOperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        generatorSeq.end();
    }
}
