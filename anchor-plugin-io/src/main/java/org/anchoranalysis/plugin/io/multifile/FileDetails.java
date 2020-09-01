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

package org.anchoranalysis.plugin.io.multifile;

import java.nio.file.Path;
import java.util.Optional;

public class FileDetails {
    private Path filePath;
    private Optional<Integer> channelNum;
    private Optional<Integer> sliceNum;
    private Optional<Integer> timeIndex;

    public FileDetails(
            Path filePath,
            Optional<Integer> channelNum,
            Optional<Integer> sliceNum,
            Optional<Integer> timeIndex) {
        super();
        this.filePath = filePath;
        this.channelNum = channelNum;
        this.sliceNum = sliceNum;
        this.timeIndex = timeIndex;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public Optional<Integer> getChannelNum() {
        return channelNum;
    }

    public void setChannelNum(Optional<Integer> channelNum) {
        this.channelNum = channelNum;
    }

    public Optional<Integer> getSliceNum() {
        return sliceNum;
    }

    public void setSliceNum(Optional<Integer> sliceNum) {
        this.sliceNum = sliceNum;
    }

    public Optional<Integer> getTimeIndex() {
        return timeIndex;
    }

    public void setTimeIndex(Optional<Integer> timeIndex) {
        this.timeIndex = timeIndex;
    }
}
