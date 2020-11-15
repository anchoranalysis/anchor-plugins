package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedRaster;
import lombok.Getter;
import lombok.Setter;

/**
 * If the extension(s) of a path matches a particular value, then use a particular reader, otherwise a fallback.
 * 
 * @author Owen Feehan
 *
 */
public class BranchExtension extends StackReader {

    // START BEAN PROPERTIES
    /** Any extensions to match (case insensitive) not including any leading period. */
    @BeanField @Getter @Setter private StringSet extensions;
    
    /** The reader to use if the extension matches. */
    @BeanField @Getter @Setter private StackReader readerMatching;

    /** The reader to use if the extension does not match. */
    @BeanField @Getter @Setter private StackReader readerNotMatching;
    // END BEAN PROPERTIES

    /** Lazily create a lowercase version of extensions, as first needed. */
    private Set<String> extensionsLowercase;
    
    @Override
    public OpenedRaster openFile(Path path) throws ImageIOException {
        if (doesPathHaveExtension(path)) {
            return readerMatching.openFile(path);
        } else {
            return readerNotMatching.openFile(path);
        }
    }
    
    private void createLowercaseExtensionsIfNecessary() {
        if (extensionsLowercase==null) {
            extensionsLowercase = extensions.stream().map(String::toLowerCase).collect(Collectors.toSet());
        }
    }
    
    private boolean doesPathHaveExtension(Path path) {
        createLowercaseExtensionsIfNecessary();
        String pathLowercase = path.toString().toLowerCase();
        return extensionsLowercase.stream().anyMatch(pathLowercase::endsWith);
    }
}
