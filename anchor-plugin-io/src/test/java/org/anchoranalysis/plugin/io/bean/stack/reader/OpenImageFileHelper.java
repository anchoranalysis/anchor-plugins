package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderOrientationCorrection;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.test.TestLoader;

/**
 * Reads image files from the {@code test/resources}.
 *
 * @author Owen Feehan
 */
class OpenImageFileHelper {

    private final TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    /** The name of the subdirectory in {@code test/resources} in which to find image-files. */
    private final String subdirectory;

    /** How to read images. */
    private final StackReaderOrientationCorrection reader;

    /**
     * Creates to read from a particular subdirectory with a particular {@link StackReader}.
     *
     * @param subdirectory the name of the subdirectory in {@code test/resources} in which to find
     *     image-files.
     * @param reader the reader to use.
     */
    public OpenImageFileHelper(String subdirectory, StackReaderOrientationCorrection reader) {
        this.subdirectory = subdirectory;
        this.reader = reader;
    }

    /**
     * Opens an image-file from the {@code subdirectory} passed to the constructor.
     *
     * @param filename the name of a file in {@code subdirectory} to open.
     * @param logger where to write informative messages to, and and any non-fatal errors (fatal errors are throw as exceptions).
     * @return the opened file.
     * @throws ImageIOException if the file cannot be opened.
     */
    public OpenedImageFile openFile(String filename, Logger logger) throws ImageIOException {
        return reader.openFile(pathForFile(filename), logger);
    }

    /**
     * Opens an image-file from the {@code subdirectory} passed to the constructor.
     *
     * @param filename the name of a file in {@code subdirectory} to open.
     * @param correction any change in orientation that should be applied after opening the image.
     * @param logger where to write informative messages to, and and any non-fatal errors (fatal errors are throw as exceptions).
     * @return the opened file.
     * @throws ImageIOException if the file cannot be opened.
     */
    public OpenedImageFile openFile(String filename, OrientationChange correction, Logger logger)
            throws ImageIOException {
        return reader.openFile(pathForFile(filename), correction, logger);
    }

    /**
     * The path to open a particular filename.
     *
     * @param filename the filename.
     * @return the path.
     */
    private Path pathForFile(String filename) {
        String relativePath = String.format("%s/%s", subdirectory, filename);
        return loader.resolveTestPath(relativePath);
    }
}
