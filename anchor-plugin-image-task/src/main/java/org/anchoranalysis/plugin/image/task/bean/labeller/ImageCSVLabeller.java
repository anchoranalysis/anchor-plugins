/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.Set;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.experiment.label.FileLabelMap;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.labeller.ImageCSVLabellerInitParams;

public class ImageCSVLabeller extends ImageLabeller<ImageCSVLabellerInitParams> {

    // START BEAN PROPERTIES
    /**
     * Path to a CSV label file (comma-separated, with header, no quotes)
     *
     * <p>The CSV file should have two columns: first column = image id (to match the
     * descriptiveName() of an image) second column = a label string
     */
    @BeanField private FilePathGenerator csvLabelFilePathGenerator;
    // END BEAN PROPERTIES

    @Override
    public ImageCSVLabellerInitParams init(Path pathForBinding) throws InitException {

        try {
            Path csvPath = csvLabelFilePathGenerator.outFilePath(pathForBinding, false);

            return new ImageCSVLabellerInitParams(FileLabelMap.readFromCSV(csvPath, false));

        } catch (CSVReaderException | AnchorIOException e) {
            throw new InitException(e);
        }
    }

    @Override
    public Set<String> allLabels(ImageCSVLabellerInitParams params) {
        return params.getLabelMap().labels();
    }

    @Override
    public String labelFor(
            ImageCSVLabellerInitParams sharedState,
            ProvidesStackInput input,
            BoundIOContext context)
            throws OperationFailedException {
        String label = sharedState.getLabelMap().get(input.descriptiveName());

        if (label == null) {
            throw new OperationFailedException(
                    String.format(
                            "No label can be found for the descriptive-name: %s",
                            input.descriptiveName()));
        }

        return label;
    }

    public FilePathGenerator getCsvLabelFilePathGenerator() {
        return csvLabelFilePathGenerator;
    }

    public void setCsvLabelFilePathGenerator(FilePathGenerator csvLabelFilePathGenerator) {
        this.csvLabelFilePathGenerator = csvLabelFilePathGenerator;
    }
}
