/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.generator.imagestack;

import ij.ImageStack;
import java.util.Optional;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.io.generator.raster.RasterGenerator;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.rgb.RGBStack;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class RasterConverterGenerator extends ImageStackGenerator {

    private RasterGenerator rasterGenerator;

    public RasterConverterGenerator(RasterGenerator rasterGenerator) {
        super();
        this.rasterGenerator = rasterGenerator;
    }

    @Override
    public ImageStack generate() throws OutputWriteFailedException {
        Stack stack = rasterGenerator.generate();
        return IJWrap.createColorProcessorStack(new RGBStack(stack));
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return rasterGenerator.createManifestDescription();
    }
}
