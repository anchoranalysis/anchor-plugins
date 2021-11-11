/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.io.bean.stack.reader;

import static org.junit.jupiter.api.Assertions.*;

import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.io.bioformats.bean.BioformatsReader;
import org.anchoranalysis.test.LoggingFixture;
import org.junit.jupiter.api.Test;

/**
 * Tests reading the {@code .flex} file format with the {@link BioformatsReader}.
 *
 * @author Owen Feehan
 */
class FlexFormatTest {

    static {
        ConfigureBioformatsLogging.instance().makeSureConfigured();
    }

    private OpenImageFileHelper loader =
            new OpenImageFileHelper("exampleFormats", new BioformatsReader());

    /**
     * Tests the numChannels and numFrames from a known file, as it sometimes incorrectly reports as
     * numChannel==1 and numFrame==1, as opposed to numFrames==2 and numChannels==1 (which is what
     * we expect... but is itself incorrect
     *
     * <p>Note that this test MIGHT only work correctly when NOT run with the GPL bioformats
     * libraries on the class-path.
     *
     * <p>Otherwise the FlexReader will be used, and its exact behaviour has yet to be established.
     *
     * @throws ImageIOException
     */
    @Test
    void testSizeCAndT() throws ImageIOException {
        OpenedImageFile openedFile =
                loader.openFile("001001007.flex", LoggingFixture.suppressedLogger());

        assertEquals(1, openedFile.numberChannels());
        assertEquals(1, openedFile.numberSeries());
        assertEquals(2, openedFile.numberFrames());
    }
}
