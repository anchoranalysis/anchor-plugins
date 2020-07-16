/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.image;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInput;

/**
 * Summarizes the size of images.
 *
 * <p>If there's more than one image in the series, the size of each is considered.
 */
public class ImageSize extends SummarizerNamedChnls<WrappedImageDim> {

    @Override
    public void add(NamedChnlsInput img) throws OperationFailedException {

        try {
            int numSeries = img.numSeries();
            for (int i = 0; i < numSeries; i++) {

                incrCount(new WrappedImageDim(img.dim(0)));
            }

        } catch (RasterIOException exc) {
            throw new OperationFailedException(exc);
        }
    }

    @Override
    protected String describeNoun() {
        return "size";
    }
}
