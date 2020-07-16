/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPP;

public class SharedObjectsFromChnlTask extends RasterTask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DefineOutputterMPP define;

    @BeanField private String outputNameOriginal = "original";
    // END BEAN PROPERTIES

    @Override
    public void doStack(
            NamedChnlsInput inputObject, int seriesIndex, int numSeries, BoundIOContext context)
            throws JobExecutionException {

        NamedChnlCollectionForSeries ncc;
        try {
            ncc = inputObject.createChnlCollectionForSeries(0, ProgressReporterNull.get());
        } catch (RasterIOException e1) {
            throw new JobExecutionException(e1);
        }

        try {
            Optional<Channel> inputImage =
                    ncc.getChnlOrNull(
                            ImgStackIdentifiers.INPUT_IMAGE, 0, ProgressReporterNull.get());
            inputImage.ifPresent(
                    image ->
                            context.getOutputManager()
                                    .getWriterCheckIfAllowed()
                                    .write(
                                            outputNameOriginal,
                                            () -> new ChnlGenerator(image, "original")));

            define.processInput(ncc, context);

        } catch (GetOperationFailedException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
}
