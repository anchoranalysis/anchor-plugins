/*-
 * #%L
 * anchor-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.bean.define;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.identifier.provider.store.SharedObjects;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.io.histogram.output.HistogramCSVGenerator;
import org.anchoranalysis.image.io.object.output.hdf5.ObjectCollectionWriter;
import org.anchoranalysis.image.io.stack.output.NamedStacksOutputter;
import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.collection.NamedProviderOutputter;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.mpp.init.MarksInitialization;
import org.anchoranalysis.mpp.mark.MarkCollection;

/**
 * Outputs entities associated with {@link SharedObjects} in particular directories.
 *
 * <p>These outputs are:
 *
 * <ul>
 *   <li>stacks
 *   <li>marks
 *   <li>histograms
 *   <li>objects
 * </ul>
 *
 * <p>Second-level output rules determine whether particular elements in each directory are written
 * or not.
 */
@AllArgsConstructor
class SharedObjectsOutputter {

    /** The shared objects to output. */
    private SharedObjects sharedObjects;

    /** Whether to suppress the creation of subfolders. */
    private boolean suppressSubfolders;

    /** The outputter to use for writing. */
    private OutputterChecked outputter;

    /**
     * Adds all possible output-names to a {@link OutputEnabledMutable}.
     *
     * @param outputEnabled where to add all possible output-names
     */
    public static void addAllOutputNamesTo(OutputEnabledMutable outputEnabled) {
        outputEnabled.addEnabledOutputFirst(
                OutputterDirectories.STACKS,
                OutputterDirectories.MARKS,
                OutputterDirectories.HISTOGRAMS,
                OutputterDirectories.OBJECTS);
    }

    /**
     * Writes (a selection of) entities from to the filesystem in particular directories.
     *
     * @throws OutputWriteFailedException if the output operation fails
     */
    public void output() throws OutputWriteFailedException {

        if (!outputter.getSettings().hasBeenInitialized()) {
            throw new OutputWriteFailedException(
                    "The Outputter's settings have not yet been initialized");
        }

        ImageInitialization initializationImage = new ImageInitialization(sharedObjects);

        stacks(initializationImage);
        histograms(initializationImage);
        objects(initializationImage);

        marks(new MarksInitialization(initializationImage));
    }

    /**
     * Outputs stacks.
     *
     * @param initialization the image initialization
     * @throws OutputWriteFailedException if the output operation fails
     */
    private void stacks(ImageInitialization initialization) throws OutputWriteFailedException {
        NamedStacksOutputter.output(
                initialization.combinedStacks(),
                OutputterDirectories.STACKS,
                suppressSubfolders,
                outputter);
    }

    /**
     * Outputs marks.
     *
     * @param initialization the marks initialization
     * @throws OutputWriteFailedException if the output operation fails
     */
    private void marks(MarksInitialization initialization) throws OutputWriteFailedException {
        output(
                initialization.marks(),
                new XStreamGenerator<MarkCollection>(),
                OutputterDirectories.MARKS);
    }

    /**
     * Outputs histograms.
     *
     * @param initialization the image initialization
     * @throws OutputWriteFailedException if the output operation fails
     */
    private void histograms(ImageInitialization initialization) throws OutputWriteFailedException {
        output(
                initialization.histograms(),
                new HistogramCSVGenerator(),
                OutputterDirectories.HISTOGRAMS);
    }

    /**
     * Outputs objects.
     *
     * @param initialization the image initialization
     * @throws OutputWriteFailedException if the output operation fails
     */
    private void objects(ImageInitialization initialization) throws OutputWriteFailedException {
        output(
                initialization.objects(),
                ObjectCollectionWriter.generator(),
                OutputterDirectories.OBJECTS);
    }

    /**
     * Outputs a named provider store using a specific generator.
     *
     * @param <T> the type of objects in the store
     * @param store the named provider store
     * @param generator the generator to use for output
     * @param directoryName the name of the directory to output to
     * @throws OutputWriteFailedException if the output operation fails
     */
    private <T> void output(
            NamedProviderStore<T> store, Generator<T> generator, String directoryName)
            throws OutputWriteFailedException {
        new NamedProviderOutputter<>(store, generator, outputter)
                .output(directoryName, suppressSubfolders);
    }
}
