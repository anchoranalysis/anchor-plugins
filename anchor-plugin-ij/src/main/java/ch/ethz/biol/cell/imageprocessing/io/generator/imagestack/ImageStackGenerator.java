/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.generator.imagestack;

import ij.ImageStack;
import java.nio.file.Path;
import org.anchoranalysis.image.io.generator.raster.RasterWriterUtilities;
import org.anchoranalysis.io.generator.ObjectGenerator;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public abstract class ImageStackGenerator extends ObjectGenerator<ImageStack> {

    @Override
    public void writeToFile(OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {
        throw new OutputWriteFailedException(
                "writing out using an ImageStackGenerator is currently disabled");
    }

    @Override
    public String getFileExtension(OutputWriteSettings outputWriteSettings) {
        return RasterWriterUtilities.getDefaultRasterFileExtension(outputWriteSettings);
    }
}
