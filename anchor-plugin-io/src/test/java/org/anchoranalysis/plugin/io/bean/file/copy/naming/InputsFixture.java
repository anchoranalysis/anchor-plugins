/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2025 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
