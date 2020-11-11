package org.anchoranalysis.plugin.mpp.experiment.bean;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.files.FileWithDirectoryInput;
import org.anchoranalysis.io.input.files.NamedFile;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.input.stack.ConvertChannelsInputToStack;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Converts an input to the expected input-type if necessary and if possible.
 * 
 * @author Owen Feehan
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ConvertInputHelper {
    
    public static <T extends NamedChannelsInput> InputFromManager convert(T input, InputTypesExpected inputTypesExpected, Optional<Path> directory) throws ExperimentExecutionException {
        Class<? extends InputFromManager> inputClass = input.getClass();
    
        if (inputTypesExpected.doesClassInheritFromAny(inputClass)) {
            // All good, the delegate happily accepts our type without change
            return input;
        } else if (inputTypesExpected.doesClassInheritFromAny(MultiInput.class)) {
            return new MultiInput(input);
        } else if (inputTypesExpected.doesClassInheritFromAny(StackSequenceInput.class)) {
            return new ConvertChannelsInputToStack(input);
        } else if (inputTypesExpected.doesClassInheritFromAny(FileWithDirectoryInput.class)) {
            return convertToFileWithDirectory(input, directory);
        
        } else {
            throw new ExperimentExecutionException(
                    String.format(
                            "Cannot pass or convert the input-type (%s) to match the delegate's expected input-type:%n%s",
                            inputClass, inputTypesExpected));
        }
    }
    
    private static <T extends NamedChannelsInput> FileWithDirectoryInput convertToFileWithDirectory(T input, Optional<Path> directory) throws ExperimentExecutionException {
        try {
            NamedFile namedFile = new NamedFile(input.name(), input.pathForBindingRequired().toFile());
            return new FileWithDirectoryInput(namedFile, directory.get());  // NOSONAR
        } catch (InputReadFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }
}
