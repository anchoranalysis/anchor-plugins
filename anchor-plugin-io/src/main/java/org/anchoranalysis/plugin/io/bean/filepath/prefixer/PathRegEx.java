/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

///

/**
 * Matches a regex against incoming file-paths to form a prefix for output
 *
 * <p>Groups (i.e. parentheses) in the regular expression each form a directory in the output
 *
 * @author Owen Feehan
 */
public class PathRegEx extends FilePathPrefixerAvoidResolve {

    // START BEAN PROPERTIES
    /** Regular expression to use to match path with at least one group in it. */
    @BeanField private RegEx regEx;
    // END BEAN PROPERTIES

    @Override
    protected FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root)
            throws FilePathPrefixerException {
        String[] components = componentsFromPath(input.getPath());
        return createPrefix(root, components);
    }

    private String[] componentsFromPath(Path pathIn) throws FilePathPrefixerException {

        String pathInForwardSlashes = FilePathToUnixStyleConverter.toStringUnixStyle(pathIn);

        return regEx.match(pathInForwardSlashes)
                .orElseThrow(
                        () ->
                                new FilePathPrefixerException(
                                        String.format(
                                                "Cannot successfully match the %s against '%s'",
                                                regEx, pathInForwardSlashes)));
    }

    /**
     * Creates a FilePathPrefix based upon an ordered array of string components
     *
     * @param outFolderPath the base folder of the prefixer
     * @param components ordered array of string components
     * @return the file-path-prefix
     */
    private static FilePathPrefix createPrefix(Path outFolderPath, String[] components) {

        Path path = outFolderPath;

        for (int g = 0; g < components.length; g++) {
            path = path.resolve(components[g]);
        }

        return new FilePathPrefix(path);
    }

    public RegEx getRegEx() {
        return regEx;
    }

    public void setRegEx(RegEx regEx) {
        this.regEx = regEx;
    }
}
