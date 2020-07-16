/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.filepath;

import java.nio.file.Path;
import org.anchoranalysis.io.error.AnchorIOException;

/** Seperates a path into two parts */
class PathTwoParts {
    private Path first;
    private Path second;

    public PathTwoParts(Path path) throws AnchorIOException {
        checkPathHasParent(path);

        if (path.endsWith("/")) {
            first = path;
            second = null;
        } else {
            first = path.getParent();
            second = path.getFileName();
        }
    }

    /** Removes the last directory from the dir */
    public void removeLastDir() {
        first = first.getParent();
    }

    /** Removes the last directory in dir to rest */
    public void moveLastDirToRest() {
        Path name = first.getFileName();
        second = name.resolve(second);
        first = first.getParent();
    }

    public Path combine() {
        return first.resolve(second);
    }

    private static void checkPathHasParent(Path path) throws AnchorIOException {

        if (path.getParent() == null) {
            throw new AnchorIOException(String.format("No parent exists for %s", path));
        }
    }

    public Path getFirst() {
        return first;
    }

    public Path getSecond() {
        return second;
    }
}
