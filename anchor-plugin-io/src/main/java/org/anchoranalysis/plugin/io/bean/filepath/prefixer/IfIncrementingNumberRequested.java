package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixer;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.prefixer.NamedPath;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerContext;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerException;
import lombok.Getter;
import lombok.Setter;

/**
 * Multiplexes between two {@link PathPrefixer}s depending on whether an <i>incrementing number sequence was output</i> was requested.
 * 
 * @author Owen Feehan
 */
public class IfIncrementingNumberRequested extends PathPrefixer {

    // START BEAN PROPERTIES
    /** Called if an incrementing-number sequence <b>was</b> requested as output. */
    @Getter @Setter @BeanField PathPrefixer prefixerIncrementingNumber;

    /** Called if an incrementing-number sequence <b>was not</b> requested as output. */
    @Getter @Setter @BeanField PathPrefixer prefixerElse;
    // END BEAN PROPERTIES
    
    @Override
    public DirectoryWithPrefix outFilePrefix(NamedPath path, String experimentIdentifier,
            PathPrefixerContext context) throws PathPrefixerException {
        return multiplex(context).outFilePrefix(path, experimentIdentifier, context);
    }

    @Override
    public DirectoryWithPrefix rootDirectoryPrefix(String experimentIdentifier,
            PathPrefixerContext context) throws PathPrefixerException {
        return multiplex(context).rootDirectoryPrefix(experimentIdentifier, context);
    }
    
    private PathPrefixer multiplex(PathPrefixerContext context) {
        if (context.isOutputIncrementingNumberSequence()) {
            return prefixerIncrementingNumber;
        } else {
            return prefixerElse;
        }
    }
}
