/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.input.stack;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.progress.CheckedProgressingSupplier;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;

/**
 * Manager that converts NamedChnlInput to StackSequenceInput
 *
 * @author Owen Feehan
 */
public class ConvertNamedChnlsToStack extends InputManager<StackSequenceInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private InputManager<NamedChnlsInput> input;

    @BeanField @Getter @Setter private String chnlName;

    @BeanField @Getter @Setter private int timeIndex = 0;
    // END BEAN PROPERTIES

    /**
     * An input object that converts StackNamedChnlInputObject to StackSequenceInputObject
     *
     * @author Owen Feehan
     */
    @AllArgsConstructor
    private class ConvertInputObject implements StackSequenceInput {

        private NamedChnlsInput in;

        @Override
        public String descriptiveName() {
            return in.descriptiveName();
        }

        @Override
        public Optional<Path> pathForBinding() {
            return in.pathForBinding();
        }

        @Override
        public void close(ErrorReporter errorReporter) {
            in.close(errorReporter);
        }

        @Override
        public CheckedProgressingSupplier<TimeSequence, OperationFailedException>
                createStackSequenceForSeries(int seriesNum) throws RasterIOException {
            return progressReporter -> convert(progressReporter, in, seriesNum);
        }

        @Override
        public void addToStoreInferNames(
                NamedProviderStore<TimeSequence> stackCollection,
                int seriesNum,
                ProgressReporter progressReporter)
                throws OperationFailedException {
            in.addToStoreInferNames(stackCollection, seriesNum, progressReporter);
        }

        @Override
        public void addToStoreWithName(
                String name,
                NamedProviderStore<TimeSequence> stackCollection,
                int seriesNum,
                ProgressReporter progressReporter)
                throws OperationFailedException {
            in.addToStoreWithName(name, stackCollection, seriesNum, progressReporter);
        }

        @Override
        public int numberFrames() throws OperationFailedException {
            return in.numberFrames();
        }
    }
    
    private TimeSequence convert(ProgressReporter progressReporter, NamedChnlsInput in, int seriesNum)
            throws OperationFailedException {

        try (ProgressReporterMultiple prm = new ProgressReporterMultiple(progressReporter, 2)) {

            NamedChannelsForSeries ncc =
                    in.createChannelsForSeries(seriesNum, new ProgressReporterOneOfMany(prm));
            prm.incrWorker();

            Channel channel =
                    ncc.getChannel(chnlName, timeIndex, new ProgressReporterOneOfMany(prm));
            return new TimeSequence(new Stack(channel));

        } catch (RasterIOException | GetOperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }
    

    @Override
    public List<StackSequenceInput> inputObjects(InputManagerParams params)
            throws AnchorIOException {
        return FunctionalList.mapToList(input.inputObjects(params), this::convert);
    }

    private StackSequenceInput convert(NamedChnlsInput in) {
        return new ConvertInputObject(in);
    }
}
