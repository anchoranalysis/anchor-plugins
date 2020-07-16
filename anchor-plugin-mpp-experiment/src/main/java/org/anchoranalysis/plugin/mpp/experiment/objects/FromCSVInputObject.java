/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.objects;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.mpp.io.input.InputForMPPBean;
import org.anchoranalysis.mpp.io.input.MultiInput;

public class FromCSVInputObject implements InputFromManager, InputForMPPBean {

    private MultiInput input;
    private Path csvFilePath;

    public FromCSVInputObject(MultiInput input, Path csvFilePath) {
        super();
        this.input = input;
        this.csvFilePath = csvFilePath;
    }

    @Override
    public String descriptiveName() {
        return input.descriptiveName();
    }

    @Override
    public Optional<Path> pathForBinding() {
        return input.pathForBinding();
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        input.close(errorReporter);
    }

    public Path getCsvFilePath() {
        return csvFilePath;
    }

    @Override
    public void addToSharedObjects(MPPInitParams soMPP, ImageInitParams soImage)
            throws OperationFailedException {
        input.addToSharedObjects(soMPP, soImage);
    }
}
