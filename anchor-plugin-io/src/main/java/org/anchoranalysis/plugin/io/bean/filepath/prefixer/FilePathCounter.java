/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

@NoArgsConstructor
public class FilePathCounter extends FilePathPrefixerAvoidResolve {

    // TODO this counter should be initialized in a proper way, and not using a bean-wide variable
    private int cnt = 0;

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int numLeadingZeros = 4;
    // END BEAN PROPERTIES

    public FilePathCounter(String outPathPrefix) {
        super(outPathPrefix);
    }

    @Override
    protected FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root) {
        Path combinedDir = root.resolve(identifier(cnt++));
        return new FilePathPrefix(combinedDir);
    }

    private String identifier(int index) {
        String formatSpecifier = "%0" + numLeadingZeros + "d";
        return String.format(formatSpecifier, index);
    }
}
