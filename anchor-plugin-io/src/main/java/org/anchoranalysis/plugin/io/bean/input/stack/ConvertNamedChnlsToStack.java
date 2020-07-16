/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.stack;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.progress.OperationWithProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
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
    private class ConvertInputObject extends StackSequenceInput {

        private NamedChnlsInput in;

        public ConvertInputObject(NamedChnlsInput in) {
            super();
            this.in = in;
        }

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
        public OperationWithProgressReporter<TimeSequence, OperationFailedException>
                createStackSequenceForSeries(int seriesNum) throws RasterIOException {
            return new OperationConvert(in, seriesNum);
        }

        @Override
        public void addToStore(
                NamedProviderStore<TimeSequence> stackCollection,
                int seriesNum,
                ProgressReporter progressReporter)
                throws OperationFailedException {
            in.addToStore(stackCollection, seriesNum, progressReporter);
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
        public int numFrames() throws OperationFailedException {
            return in.numFrames();
        }
    }

    /**
     * The operation of doing the conversion
     *
     * @author Owen Feehan
     */
    private class OperationConvert
            implements OperationWithProgressReporter<TimeSequence, OperationFailedException> {

        private int seriesNum;
        private NamedChnlsInput in;

        public OperationConvert(NamedChnlsInput in, int seriesNum) {
            super();
            this.seriesNum = seriesNum;
            this.in = in;
        }

        @Override
        public TimeSequence doOperation(ProgressReporter progressReporter)
                throws OperationFailedException {

            try (ProgressReporterMultiple prm = new ProgressReporterMultiple(progressReporter, 2)) {

                NamedChnlCollectionForSeries ncc =
                        in.createChnlCollectionForSeries(
                                seriesNum, new ProgressReporterOneOfMany(prm));
                prm.incrWorker();
                Channel chnl = ncc.getChnl(chnlName, timeIndex, new ProgressReporterOneOfMany(prm));

                TimeSequence ts = new TimeSequence();
                ts.add(new Stack(chnl));
                return ts;
            } catch (RasterIOException | GetOperationFailedException e) {
                throw new OperationFailedException(e);
            }
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
