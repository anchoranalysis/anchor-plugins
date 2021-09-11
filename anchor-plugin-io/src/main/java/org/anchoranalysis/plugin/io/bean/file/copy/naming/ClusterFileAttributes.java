package org.anchoranalysis.plugin.io.bean.file.copy.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.input.path.CopyContext;
import org.anchoranalysis.plugin.io.shared.AnonymizeSharedState;


/**
 * Clusters the files based upon various file attributes into different subdirectories.
 * 
 * <p>The subdirectories are named 01, 02, 03 etc. depending on the number of clusters.
 * 
 * <p>The relative-path of files are preserved, being added relative to the cluster subdirectory.
 * 
 * @author Owen Feehan
 *
 */
public class ClusterFileAttributes extends CopyFilesNaming<AnonymizeSharedState> {

    @Override
    public AnonymizeSharedState beforeCopying(Path destinationDirectory, int totalNumberFiles) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Path> destinationPathRelative(File file, DirectoryWithPrefix outputTarget,
            int index, CopyContext<AnonymizeSharedState> context)
            throws OutputWriteFailedException {
        // TODO Auto-generated method stub
        return null;
    }

}
