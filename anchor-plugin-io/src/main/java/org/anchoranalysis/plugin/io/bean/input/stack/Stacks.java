/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.io.bean.input.stack;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.bean.stack.reader.InputManagerWithStackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.file.FileInput;

/**
 * An {@link org.anchoranalysis.io.input.bean.InputManager} where each file provides one or more
 * {@link Stack}s.
 *
 * <p>Specifically, each file provides either a single {@link Stack} or a time-series of {@link
 * Stack}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class Stacks extends InputManagerWithStackReader<StackSequenceInput> {

    // START BEANS
    @BeanField @Getter @Setter private InputManager<FileInput> fileInput;

    @BeanField @Getter @Setter private boolean useLastSeriesIndexOnly;

    // END BEANS

    public Stacks(InputManager<FileInput> fileInput) {
        this.fileInput = fileInput;
    }

    @Override
    public InputsWithDirectory<StackSequenceInput> inputs(InputManagerParameters parameters)
            throws InputReadFailedException {
        return fileInput
                .inputs(parameters)
                .map(
                        file ->
                                new StackCollectionFromFilesInputObject(
                                        file,
                                        getStackReader(),
                                        useLastSeriesIndexOnly,
                                        parameters.getExecutionTimeRecorder(),
                                        parameters.getLogger()));
    }
}
