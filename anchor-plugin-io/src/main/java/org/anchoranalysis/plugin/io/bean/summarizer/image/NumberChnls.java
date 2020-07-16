/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.image;

import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInput;

public class NumberChnls extends SummarizerNamedChnlsSimple<Integer> {

    @Override
    protected Integer extractKey(NamedChnlsInput element) throws RasterIOException {
        return element.numChnl();
    }

    @Override
    protected String describeNoun() {
        return "channel";
    }
}
