/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;

public abstract class CopyFilesNamingOneRegEx extends CopyFilesNamingOne {

    // START BEAN PROPERTIES
    @BeanField private String regex;
    // END BEAN PROPERTIES

    @Override
    public Optional<Path> destinationPathRelative(Path pathDelegate) throws AnchorIOException {
        return destinationPathRelative(pathDelegate, regex);
    }

    protected abstract Optional<Path> destinationPathRelative(Path pathDelegate, String regex)
            throws AnchorIOException;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
