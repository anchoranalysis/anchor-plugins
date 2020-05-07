package anchor.plugin.ij.task;

/*
 * #%L
 * anchor-plugin-ij
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.nio.ByteBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.bound.BoundIOContext;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderIJBackgroundSubtractor;

public class BackgroundSubtractShortTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int radius;
	
	@BeanField
	private int scaleDownIntensityFactor = 1;
	// END BEAN PROPERTIES
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
}

	@Override
	public void startSeries(BoundOutputManagerRouteErrors outputManager,
			ErrorReporter errorReporter) throws JobExecutionException {
		
	}

	@Override
	public void doStack(NamedChnlsInput inputObject,
			int seriesIndex, int numSeries,
			BoundIOContext context) throws JobExecutionException {
		
		ProgressReporter progressReporter = ProgressReporterNull.get();
		
		try {
			NamedChnlCollectionForSeries ncc = inputObject.createChnlCollectionForSeries(0, progressReporter );
			
			Chnl inputImage = ncc.getChnlOrNull(ImgStackIdentifiers.INPUT_IMAGE, 0, progressReporter);
			
			Chnl bgSubOut = ChnlProviderIJBackgroundSubtractor.subtractBackground(inputImage, radius, false );
			VoxelBox<?> vbSubOut = bgSubOut.getVoxelBox().any();
			
			double maxPixel = vbSubOut.ceilOfMaxPixel();
			
			double scaleRatio = 255.0/maxPixel;
			
			// We go from 2048 to 256
			if (scaleDownIntensityFactor!=1) {
				vbSubOut.multiplyBy(scaleRatio);
			}
			
			ChnlConverter<ByteBuffer> converter = new ChnlConverterToUnsignedByte();
			Chnl chnlOut = converter.convert(bgSubOut,ConversionPolicy.CHANGE_EXISTING_CHANNEL);
			
			context.getOutputManager().getWriterCheckIfAllowed().write(
				"bgsub",
				() -> new ChnlGenerator(chnlOut, "imgChnl")
			);
			
		} catch (RasterIOException | CreateException | GetOperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public void endSeries(BoundOutputManagerRouteErrors outputManager)
			throws JobExecutionException {
		
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
