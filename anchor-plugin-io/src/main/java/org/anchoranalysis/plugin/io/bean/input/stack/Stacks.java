/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.stack;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;

/**
 * Each file gives either: * a single stack * a time-series of stacks
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class Stacks extends InputManager<StackSequenceInput> {

    // START BEANS
    @BeanField @Getter @Setter private InputManager<FileInput> fileInput;

    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    @BeanField @Getter @Setter private boolean useLastSeriesIndexOnly;
    // END BEANS

    public Stacks(InputManager<FileInput> fileInput) {
        this.fileInput = fileInput;
    }

    @Override
    public List<StackSequenceInput> inputObjects(InputManagerParams params)
            throws AnchorIOException {
        return FunctionalList.mapToList(
                fileInput.inputObjects(params),
                file ->
                        new StackCollectionFromFilesInputObject(
                                file, getRasterReader(), useLastSeriesIndexOnly));
    }
}
