/*-
 * #%L
 * anchor-plugin-image-task
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
/* (C)2020 */
package org.anchoranalysis.plugin.image.task.feature;

import java.util.Optional;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;

public class GenerateHeadersForCSV {

    private final String[] results;
    private final Optional<String> additionalGroupHeader;

    public GenerateHeadersForCSV(String[] results, Optional<String> additionalGroupHeader) {
        super();
        this.results = results;
        this.additionalGroupHeader = additionalGroupHeader;
    }

    public MetadataHeaders createMetadataHeaders(boolean groupGeneratorDefined) {
        return new MetadataHeaders(headersForGroup(groupGeneratorDefined), results);
    }

    private String[] headersForGroup(boolean groupGeneratorDefined) {
        if (groupGeneratorDefined) {
            if (additionalGroupHeader.isPresent()) {
                return new String[] {"group", additionalGroupHeader.get()};
            } else {
                return new String[] {"group"};
            }
        } else {
            if (additionalGroupHeader.isPresent()) {
                return new String[] {additionalGroupHeader.get()};
            } else {
                return new String[] {};
            }
        }
    }
}
