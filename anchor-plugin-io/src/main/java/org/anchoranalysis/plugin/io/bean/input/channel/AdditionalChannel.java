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

package org.anchoranalysis.plugin.io.bean.input.channel;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.image.io.bean.channel.map.ChannelEntry;
import org.anchoranalysis.image.io.channel.NamedEntries;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.PathSupplier;

@AllArgsConstructor
class AdditionalChannel {

    /** Name of additional channel */
    @Getter private String name;
    
    /** Index of additional channe; */
    private int index;
    
    /** Supplies a file-path associated with the additional channel */
    private PathSupplier filePath;

    public NamedEntries createChannelMap() {
        NamedEntries map = new NamedEntries();
        map.add(new ChannelEntry(name, index));
        return map;
    }

    /**
     * A file-path associated with the additional channel
     * 
     * @return the file-path
     * @throws AnchorIOException
     */
    public Path getFilePath() throws AnchorIOException {
        return filePath.get();
    }
}
