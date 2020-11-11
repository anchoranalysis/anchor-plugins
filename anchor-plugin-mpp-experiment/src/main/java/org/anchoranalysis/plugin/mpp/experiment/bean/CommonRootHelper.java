package org.anchoranalysis.plugin.mpp.experiment.bean;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.io.input.InputFromManager;
import com.owenfeehan.pathpatternfinder.commonpath.FindCommonPathElements;
import com.owenfeehan.pathpatternfinder.commonpath.PathElements;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class CommonRootHelper {

    /**
     * Finds a maximal common path that is the parent of the {@link InputFromManager#pathForBinding()} associated with each input.
     * 
     * @param <T> input-type in the list
     * @param inputs the inputs
     * @return a path that is the parent of all files
     * @throws ExperimentExecutionException if no such path can be identified.
     */
    public static <T extends NamedChannelsInput> Path findCommonPathRoot(List<T> inputs) throws ExperimentExecutionException {
        return findCommonPathRootOptional(inputs).orElseThrow( () -> new ExperimentExecutionException("No common root exists for the paths, so cannot copy") );
    }
        
    /** Finds the common root of a list of inputs. */
    private static <T extends NamedChannelsInput> Optional<Path> findCommonPathRootOptional(List<T> inputs) {
        
        List<Path> paths = extractPaths(inputs);

        if (paths.size()==1) {
            return Optional.ofNullable(paths.get(0).getParent());
        } else {
            return FindCommonPathElements.findForFilePaths(paths).map(PathElements::toPath);
        }
    }
    
    private static <T extends NamedChannelsInput> List<Path> extractPaths(List<T> inputs) {
        return inputs.stream().map(InputFromManager::pathForBinding).filter(Optional::isPresent).map(Optional::get).map(CommonRootHelper::normalizePath).collect(Collectors.toList());
    }
    
    private static Path normalizePath( Path pathUnormalized ) {
        return pathUnormalized.toAbsolutePath().normalize();
    }
}
