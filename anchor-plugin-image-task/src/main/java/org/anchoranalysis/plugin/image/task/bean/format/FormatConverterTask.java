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

package org.anchoranalysis.plugin.image.task.bean.format;

import java.util.Set;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.progress.ProgressReporterConsole;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelTo;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.channel.ChnlFilter;
import org.anchoranalysis.image.io.chnl.ChannelGetter;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalRerouterErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.namestyle.StringSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.chnl.conversionstyle.ChnlConversionStyle;
import org.anchoranalysis.plugin.image.task.chnl.convert.ChnlGetterForTimepoint;
import lombok.Getter;
import lombok.Setter;

/**
 * Converts the input-image to the default output format, optionally changing the bit depth
 *
 * <p>If it looks like an RGB image, channels are written together. Otherwise they are written
 * independently
 *
 * @author Owen Feehan
 */
public class FormatConverterTask extends RasterTask {

    // START BEAN PROPERTIES

    /** To convert as RGB or independently or in another way */
    @BeanField @Getter @Setter private ChnlConversionStyle chnlConversionStyle = null;

    @BeanField @Getter @Setter private boolean suppressSeries = false;

    @BeanField @OptionalBean @Getter @Setter private ChnlFilter chnlFilter = null;

    @BeanField @OptionalBean @Getter @Setter private ConvertChannelTo chnlConverter = null;
    // END BEAN PROPERTIES

    private GeneratorSequenceNonIncrementalRerouterErrors<Stack> generatorSeq;

    public FormatConverterTask() {
        super();
    }

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {

        StackGenerator generator = new StackGenerator(false, "out");

        generatorSeq =
                new GeneratorSequenceNonIncrementalRerouterErrors<>(
                        new GeneratorSequenceNonIncrementalWriter<>(
                                outputManager.getDelegate(),
                                "",
                                // NOTE WE ARE NOT ASSIGNING A NAME TO THE OUTPUT
                                new StringSuffixOutputNameStyle("", "%s"),
                                generator,
                                true),
                        errorReporter);
        generatorSeq.setSuppressSubfolder(true);

        // TODO it would be nicer to reflect the real sequence type, than just using a set of
        // indexes
        generatorSeq.start(new SetSequenceType(), -1);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    public NamedChannelsForSeries createChnlCollection(
            NamedChnlsInput inputObject, int seriesIndex) throws RasterIOException {
        return inputObject.createChannelsForSeries(
                seriesIndex, new ProgressReporterConsole(1));
    }

    @Override
    public void doStack(
            NamedChnlsInput inputObjectUntyped,
            int seriesIndex,
            int numSeries,
            BoundIOContext context)
            throws JobExecutionException {

        try {
            NamedChannelsForSeries chnlCollection =
                    createChnlCollection(inputObjectUntyped, seriesIndex);

            ChannelGetter chnlGetter = maybeAddFilter(chnlCollection, context);

            if (chnlConverter != null) {
                chnlGetter = maybeAddConverter(chnlGetter);
            }

            convertEachTimepoint(
                    seriesIndex,
                    chnlCollection.channelNames(),
                    numSeries,
                    chnlCollection.sizeT(ProgressReporterNull.get()),
                    chnlGetter,
                    context.getLogger());

        } catch (RasterIOException | CreateException | AnchorIOException e) {
            throw new JobExecutionException(e);
        }
    }

    private void convertEachTimepoint(
            int seriesIndex,
            Set<String> chnlNames,
            int numSeries,
            int sizeT,
            ChannelGetter chnlGetter,
            Logger logger)
            throws AnchorIOException {

        for (int t = 0; t < sizeT; t++) {

            CalcOutputName calcOutputName =
                    new CalcOutputName(seriesIndex, numSeries, t, sizeT, suppressSeries);

            logger.messageLogger().logFormatted("Starting time-point: %d", t);

            ChnlGetterForTimepoint getterForTimepoint = new ChnlGetterForTimepoint(chnlGetter, t);

            chnlConversionStyle.convert(
                    chnlNames,
                    getterForTimepoint,
                    (name, stack) -> addStackToOutput(name, stack, calcOutputName),
                    logger);

            logger.messageLogger().logFormatted("Ending time-point: %d", t);
        }
    }

    private void addStackToOutput(String name, Stack stack, CalcOutputName calcOutputName) {
        generatorSeq.add(stack, calcOutputName.calcOutputName(name));
    }

    private ChannelGetter maybeAddConverter(ChannelGetter chnlGetter) throws CreateException {
        if (chnlConverter != null) {
            return new ConvertingChnlCollection(
                    chnlGetter,
                    chnlConverter.createConverter(),
                    ConversionPolicy.CHANGE_EXISTING_CHANNEL);
        } else {
            return chnlGetter;
        }
    }

    private ChannelGetter maybeAddFilter(
            NamedChannelsForSeries chnlCollection, BoundIOContext context) {

        if (chnlFilter != null) {

            chnlFilter.init((NamedChannelsForSeries) chnlCollection, context);
            return chnlFilter;
        } else {
            return chnlCollection;
        }
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        generatorSeq.end();
    }
}
