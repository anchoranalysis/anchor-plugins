package org.anchoranalysis.plugin.io.bean.file.copy.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.input.file.NamedFile;

/** Creates a list of {@link FileWithDirectoryInput} in a particular directory. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InputsFixture {

    /**
     * Create a list of inputs in a particular directory.
     *
     * <p>Specifically, 3 inputs for files: a.png, b.png, c.png are created.
     *
     * @param directory the directory in which the inputs will be created.
     */
    public static List<FileWithDirectoryInput> createInputs(Path directory) {
        List<FileWithDirectoryInput> inputs = new ArrayList<>();
        inputs.add(createInput("a", directory));
        inputs.add(createInput("b", directory));
        inputs.add(createInput("c", directory));
        return inputs;
    }

    /** Create a single input. */
    private static FileWithDirectoryInput createInput(String name, Path directory) {
        File file = directory.resolve(name + ".png").toFile();
        return new FileWithDirectoryInput(new NamedFile(name, file), directory);
    }
}
