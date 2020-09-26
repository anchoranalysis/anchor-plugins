package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.OneOrThreeChannelsWriter;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.bufferedimage.BufferedImageFactory;

public class ImageIOWriter extends OneOrThreeChannelsWriter {

    @Override
    protected void writeStackAfterCheck(Stack stack, Path filePath) throws RasterIOException {
        try {
            ImageIO.write(BufferedImageFactory.create(stack), getExtension(), filePath.toFile());
        } catch (CreateException | IOException e) {
            throw new RasterIOException(e);
        }
    }
}
