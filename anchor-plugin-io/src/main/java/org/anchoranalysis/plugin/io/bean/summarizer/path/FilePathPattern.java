/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.path;

import com.owenfeehan.pathpatternfinder.PathPatternFinder;
import com.owenfeehan.pathpatternfinder.Pattern;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.apache.commons.io.IOCase;

/**
 * Converts a list of file-paths into a form that tries to find a pattern in the naming style using
 * the path-pattern-finder library
 */
public class FilePathPattern extends Summarizer<Path> {

    // START BEAN PROPERTIES
    /** Iff TRUE, any hidden-path is not considered, and simply ignored */
    private boolean ignoreHidden = true;

    /**
     * if TRUE, case is ignored in the pattern matching. Otherwise the system-default is used i.e.
     * Windows ingores case, Linux doesn't
     */
    private boolean ignoreCase = false;
    // END BEAN PROPERTIES

    private List<Path> paths = new ArrayList<>();

    @Override
    public synchronized void add(Path element) throws OperationFailedException {
        try {
            if (acceptPath(element)) {
                paths.add(element);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    private boolean acceptPath(Path path) throws IOException {
        // Always accept the path if it doesn't exist on the file-system
        return !path.toFile().exists() || !ignoreHidden || !Files.isHidden(path);
    }

    @Override
    public synchronized String describe() throws OperationFailedException {

        if (paths.isEmpty()) {
            throw new OperationFailedException("There are no paths to summarize");
        }

        if (paths.size() == 1) {
            // There is no pattern possible, so just return the path as is
            return paths.get(0).toString();
        }

        Pattern pattern = PathPatternFinder.findPatternPath(paths, selectIOCase());
        return pattern.describeDetailed();
    }

    private IOCase selectIOCase() {
        return ignoreCase ? IOCase.INSENSITIVE : IOCase.SYSTEM;
    }

    public boolean isIgnoreHidden() {
        return ignoreHidden;
    }

    public void setIgnoreHidden(boolean ignoreHidden) {
        this.ignoreHidden = ignoreHidden;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
}
