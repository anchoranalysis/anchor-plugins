/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.labeller.FileLabelMap;
import org.anchoranalysis.plugin.image.task.labeller.ImageCSVLabellerInitialization;

/** Labels images based on a CSV file containing image identifiers and corresponding labels. */
public class ImageCSVLabeller extends ImageLabeller<ImageCSVLabellerInitialization> {

    // START BEAN PROPERTIES
    /**
     * Path to a CSV label file (comma-separated, with header, no quotes).
     *
     * <p>The CSV file should have two columns: first column = image id (to match the input-name of
     * an image) second column = a label string
     */
    @BeanField @Getter @Setter private DerivePath pathLabel;

    // END BEAN PROPERTIES

    @Override
    public ImageCSVLabellerInitialization initialize(Path pathForBinding)
            throws InitializeException {

        try {
            Path csvPath = pathLabel.deriveFrom(pathForBinding, false);

            return new ImageCSVLabellerInitialization(FileLabelMap.readFromCSV(csvPath, false));

        } catch (CSVReaderException | DerivePathException e) {
            throw new InitializeException(e);
        }
    }

    @Override
    public Set<String> allLabels(ImageCSVLabellerInitialization initialization) {
        return initialization.getLabelMap().labels();
    }

    @Override
    public String labelFor(
            ImageCSVLabellerInitialization sharedState,
            ProvidesStackInput input,
            InputOutputContext context)
            throws OperationFailedException {
        String label = sharedState.getLabelMap().get(input.identifier());

        if (label == null) {
            throw new OperationFailedException(
                    String.format("No label can be found for the name: %s", input.identifier()));
        }

        return label;
    }
}
