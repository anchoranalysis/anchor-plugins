/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles.parser;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;

public abstract class FilePathParser extends AnchorBean<FilePathParser> {

    public abstract boolean setPath(String path);

    public abstract Optional<Integer> getChnlNum();

    public abstract Optional<Integer> getZSliceNum();

    public abstract Optional<Integer> getTimeIndex();

    public abstract String getKey();
}
