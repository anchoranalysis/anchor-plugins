/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

import java.nio.file.Path;
import java.util.Optional;

public class FileDetails {
    private Path filePath;
    private Optional<Integer> chnlNum;
    private Optional<Integer> sliceNum;
    private Optional<Integer> timeIndex;

    public FileDetails(
            Path filePath,
            Optional<Integer> chnlNum,
            Optional<Integer> sliceNum,
            Optional<Integer> timeIndex) {
        super();
        this.filePath = filePath;
        this.chnlNum = chnlNum;
        this.sliceNum = sliceNum;
        this.timeIndex = timeIndex;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public Optional<Integer> getChnlNum() {
        return chnlNum;
    }

    public void setChnlNum(Optional<Integer> chnlNum) {
        this.chnlNum = chnlNum;
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
