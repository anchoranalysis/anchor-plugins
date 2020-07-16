/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;

//
public class HomeSubdirectory extends FilePathPrefixer {

    // START PROPERTIES
    /** A relative-path (to the user home directory) which is used as an output directory */
    @BeanField @Getter @Setter private String directory = "anchorData";
    // END PROPERTIES

    // If delegate is null, it means it hasn't been initialised yet
    private FilePathCounter delegate;

    private void initIfPossible() throws InitException {
        if (delegate == null) {

            Path pathAnchorDir = createSubdirectoryIfNecessary(homeDir(), directory);

            delegate = new FilePathCounter(pathAnchorDir.toString());

            // We localize instead to the home sub-directory, not to the current bean location
            try {
                delegate.localise(pathAnchorDir);
            } catch (BeanMisconfiguredException e) {
                throw new InitException(e);
            }
        }
    }

    @Override
    public FilePathPrefix outFilePrefix(
            PathWithDescription input, String expName, FilePathPrefixerParams context)
            throws FilePathPrefixerException {
        try {
            initIfPossible();
        } catch (InitException e) {
            throw new FilePathPrefixerException(e);
        }
        return delegate.outFilePrefix(input, expName, context);
    }

    @Override
    public FilePathPrefix rootFolderPrefix(String expName, FilePathPrefixerParams context)
            throws FilePathPrefixerException {
        try {
            initIfPossible();
        } catch (InitException e) {
            throw new FilePathPrefixerException(e);
        }
        return delegate.rootFolderPrefix(expName, context);
    }

    private Path homeDir() throws InitException {
        String strHomeDir = System.getProperty("user.home");

        if (strHomeDir == null || strHomeDir.isEmpty()) {
            throw new InitException("No user.home environmental variable");
        }

        Path pathHomeDir = Paths.get(strHomeDir);

        if (!pathHomeDir.toFile().exists()) {
            throw new InitException(String.format("User home dir '%s' does not exist", strHomeDir));
        }

        return pathHomeDir;
    }

    private Path createSubdirectoryIfNecessary(Path pathHomeDir, String relativePathSubdirectory)
            throws InitException {
        Path resolvedPath = pathHomeDir.resolve(relativePathSubdirectory);
        try {
            Files.createDirectory(resolvedPath);
        } catch (IOException e) {
            throw new InitException(e);
        }
        return resolvedPath;
    }
}
