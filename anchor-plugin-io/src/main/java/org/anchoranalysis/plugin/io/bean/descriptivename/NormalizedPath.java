/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFileIndependent;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;

/**
 * Uses the normalized path (always with forward slashes) of each file as it's descriptive-name
 *
 * @author Owen Feehan
 */
public class NormalizedPath extends DescriptiveNameFromFileIndependent {

    @Override
    protected String createDescriptiveName(File file, int index) throws CreateException {
        return FilePathToUnixStyleConverter.toStringUnixStyle(file.toPath().normalize().toString());
    }
}
