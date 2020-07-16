/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.generator.imagestack;

import ij.ImageStack;
import java.util.Optional;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.rgb.RGBStack;
import org.anchoranalysis.io.generator.IterableObjectGenerator;
import org.anchoranalysis.io.generator.ObjectGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Converts a RasterGenerator into a ImageStackGenerator
 *
 * @author Owen Feehan
 * @param <T> iteration-type
 */
public class RasterConverterIterableGenerator<T> extends ImageStackGenerator
        implements IterableObjectGenerator<T, ImageStack> {

    private IterableObjectGenerator<T, Stack> rasterGenerator;

    public RasterConverterIterableGenerator(IterableObjectGenerator<T, Stack> rasterGenerator) {
        super();
        this.rasterGenerator = rasterGenerator;
    }

    @Override
    public ImageStack generate() throws OutputWriteFailedException {
        Stack stack = rasterGenerator.getGenerator().generate();
        return IJWrap.createColorProcessorStack(new RGBStack(stack));
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return rasterGenerator.getGenerator().createManifestDescription();
    }

    @Override
    public void start() throws OutputWriteFailedException {
        rasterGenerator.start();
    }

    @Override
    public void end() throws OutputWriteFailedException {
        rasterGenerator.end();
    }

    @Override
    public T getIterableElement() {
        return rasterGenerator.getIterableElement();
    }

    @Override
    public void setIterableElement(T element) throws SetOperationFailedException {
        this.rasterGenerator.setIterableElement(element);
    }

    @Override
    public ObjectGenerator<ImageStack> getGenerator() {
        return this;
    }
}
