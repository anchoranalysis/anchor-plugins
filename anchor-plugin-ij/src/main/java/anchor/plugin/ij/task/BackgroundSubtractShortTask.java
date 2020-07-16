/* (C)2020 */
package anchor.plugin.ij.task;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderIJBackgroundSubtractor;
import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

public class BackgroundSubtractShortTask extends RasterTask {

    // START BEAN PROPERTIES
    @BeanField private int radius;

    @BeanField private int scaleDownIntensityFactor = 1;
    // END BEAN PROPERTIES

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void doStack(
            NamedChnlsInput inputObject, int seriesIndex, int numSeries, BoundIOContext context)
            throws JobExecutionException {

        ProgressReporter progressReporter = ProgressReporterNull.get();

        try {
            NamedChnlCollectionForSeries ncc =
                    inputObject.createChnlCollectionForSeries(0, progressReporter);

            Channel inputImage = ncc.getChnl(ImgStackIdentifiers.INPUT_IMAGE, 0, progressReporter);

            Channel bgSubOut =
                    ChnlProviderIJBackgroundSubtractor.subtractBackground(
                            inputImage, radius, false);
            VoxelBox<?> vbSubOut = bgSubOut.getVoxelBox().any();

            double maxPixel = vbSubOut.ceilOfMaxPixel();

            double scaleRatio = 255.0 / maxPixel;

            // We go from 2048 to 256
            if (scaleDownIntensityFactor != 1) {
                vbSubOut.multiplyBy(scaleRatio);
            }

            ChannelConverter<ByteBuffer> converter = new ChannelConverterToUnsignedByte();
            Channel chnlOut = converter.convert(bgSubOut, ConversionPolicy.CHANGE_EXISTING_CHANNEL);

            context.getOutputManager()
                    .getWriterCheckIfAllowed()
                    .write("bgsub", () -> new ChnlGenerator(chnlOut, "imgChnl"));

        } catch (RasterIOException | GetOperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getScaleDownIntensityFactor() {
        return scaleDownIntensityFactor;
    }

    public void setScaleDownIntensityFactor(int scaleDownIntensityFactor) {
        this.scaleDownIntensityFactor = scaleDownIntensityFactor;
    }
}
