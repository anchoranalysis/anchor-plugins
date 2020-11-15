package org.anchoranalysis.plugin.opencv.bean.stack;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.TimeSequence;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.OpenedRaster;
import org.anchoranalysis.plugin.opencv.convert.ConvertFromMat;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import lombok.RequiredArgsConstructor;

/**
 * An implementation of {@link OpenedRaster} for reading using OpenCV.
 * 
 * @author Owen Feehan
 *
 */
@RequiredArgsConstructor class OpenedRasterOpenCV implements OpenedRaster {

    private static final List<String> RGB_CHANNEL_NAMES = Arrays.asList("red", "green", "blue");
    
    // START REQUIRED ARGUMENTS
    /** The path to open. */
    private final Path path;
    // END REQUIRED ARGUMENTS
    
    /** Lazily opened stack. */
    private Stack stack;
    
    @Override
    public TimeSequence open(int seriesIndex, Progress progress) throws ImageIOException {
        openStackIfNecessary();
        return new TimeSequence(stack);
    }

    @Override
    public int numberSeries() {
        return 1;
    }

    @Override
    public Optional<List<String>> channelNames() throws ImageIOException {
        openStackIfNecessary();
        return OptionalUtilities.createFromFlag(stack.isRGB(), RGB_CHANNEL_NAMES);
    }

    @Override
    public int numberChannels() throws ImageIOException {
        openStackIfNecessary();
        return stack.getNumberChannels();
    }

    @Override
    public int numberFrames() throws ImageIOException {
        return 1;
    }

    @Override
    public int bitDepth() throws ImageIOException {
        openStackIfNecessary();
        if (!stack.allChannelsHaveIdenticalType()) {
            throw new ImageIOException("Not all channels have identical channel type, so not calculating bit-depth.");
        }
        return stack.getChannel(0).getVoxelDataType().numberBits();
    }

    @Override
    public boolean isRGB() throws ImageIOException {
        openStackIfNecessary();
        return stack.isRGB();
    }

    @Override
    public void close() throws ImageIOException {
        stack = null;
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex) throws ImageIOException {
        openStackIfNecessary();
        return stack.dimensions();
    }
    
    /** Opens the stack if has not already been opened. */
    private void openStackIfNecessary() throws ImageIOException {
        Mat image = Imgcodecs.imread(path.toString());
        
        try {
            stack = ConvertFromMat.toStack(image);
        } catch (OperationFailedException e) {
            throw new ImageIOException(e);
        }
    }
}