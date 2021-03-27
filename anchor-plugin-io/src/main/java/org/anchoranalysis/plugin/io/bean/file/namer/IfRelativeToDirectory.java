package org.anchoranalysis.plugin.io.bean.file.namer;

import java.io.File;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;

/**
 * Multiplexes between two namers depending on if the <i>relative-to-directory</i> option is
 * selected.
 *
 * @author Owen Feehan
 */
public class IfRelativeToDirectory extends FileNamer {

    // START BEAN PROPERTIES
    /** The namer to use if the <i>relative-to-directory</i> option <b>is selected</b>. */
    @BeanField @Getter @Setter private FileNamer whenRelative;

    /** The namer to use if the <i>relative-to-directory</i> option <b>is not selected</b>. */
    @BeanField @Getter @Setter private FileNamer otherwise;
    // END BEAN PROPERTIES

    @Override
    public List<NamedFile> deriveName(Collection<File> files, FileNamerContext context) {
        if (context.isRelativeToDirectory()) {
            return whenRelative.deriveName(files, context);
        } else {
            return otherwise.deriveName(files, context);
        }
    }
}
