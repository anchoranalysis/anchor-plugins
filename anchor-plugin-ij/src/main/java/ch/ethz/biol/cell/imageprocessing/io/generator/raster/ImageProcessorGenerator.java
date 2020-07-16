/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.generator.raster;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.generator.SingleFileTypeGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class ImageProcessorGenerator extends SingleFileTypeGenerator {

    private ImageProcessor imageProcessor;

    private String manifestDescriptionFunction = "undefined";

    public ImageProcessorGenerator(
            ImageProcessor imageProcessor, String manifestDescriptionFunction) {
        super();
        this.imageProcessor = imageProcessor;
        this.manifestDescriptionFunction = manifestDescriptionFunction;
    }

    @Override
    public void writeToFile(OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {

        ImagePlus ip = new ImagePlus("file", imageProcessor);

        FileSaver fs = new FileSaver(ip);
        fs.saveAsTiff(filePath.toString());
    }

    @Override
    public String getFileExtension(OutputWriteSettings outputWriteSettings) {
        return "tif";
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return Optional.of(new ManifestDescription("raster", manifestDescriptionFunction));
    }
}
