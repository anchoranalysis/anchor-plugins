/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.chnl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.cache.CachedOperation;
import org.anchoranalysis.core.functional.FunctionalProgress;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.OperationOutFilePath;

public class NamedChnlsAppend extends NamedChnlsBase {

    // START BEAN PROPERTIES
    @BeanField private InputManager<NamedChnlsInputPart> input;

    @BeanField @DefaultInstance private RasterReader rasterReader;

    @BeanField @OptionalBean private List<NamedBean<FilePathGenerator>> listAppend;

    @BeanField private boolean forceEagerEvaluation = false;

    @BeanField private boolean ignoreFileNotFoundAppend = false;

    @BeanField private boolean skipMissingChannels = false;
    // END BEAN PROPERTIES

    @Override
    public List<NamedChnlsInputPart> inputObjects(InputManagerParams params)
            throws AnchorIOException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), 2)) {

            Iterator<NamedChnlsInputPart> itr = input.inputObjects(params).iterator();

            prm.incrWorker();

            List<NamedChnlsInputPart> listTemp = new ArrayList<>();
            while (itr.hasNext()) {
                listTemp.add(itr.next());
            }

            List<NamedChnlsInputPart> outList =
                    createOutList(
                            listTemp,
                            new ProgressReporterOneOfMany(prm),
                            params.isDebugModeActivated());

            prm.incrWorker();

            return outList;
        }
    }

    private List<NamedChnlsInputPart> createOutList(
            List<NamedChnlsInputPart> listTemp,
            ProgressReporter progressReporter,
            boolean debugMode)
            throws AnchorIOException {
        return FunctionalProgress.mapListOptional(
                listTemp, progressReporter, ncc -> maybeAppend(ncc, debugMode));
    }

    private Optional<NamedChnlsInputPart> maybeAppend(
            final NamedChnlsInputPart ncc, boolean debugMode) throws AnchorIOException {
        if (ignoreFileNotFoundAppend) {

            try {
                return Optional.of(append(ncc, debugMode));
            } catch (AnchorIOException e) {
                return Optional.empty();
            }

        } else {
            return Optional.of(append(ncc, debugMode));
        }
    }

    // We assume all the input files are single channel images
    private NamedChnlsInputPart append(final NamedChnlsInputPart ncc, boolean debugMode)
            throws AnchorIOException {

        NamedChnlsInputPart out = ncc;

        if (listAppend == null) {
            return out;
        }

        for (final NamedBean<FilePathGenerator> ni : listAppend) {

            // Delayed-calculation of the appending path as it can be a bit expensive when
            // multiplied by so many items
            CachedOperation<Path, AnchorIOException> outPath =
                    new OperationOutFilePath(ni, ncc::pathForBinding, debugMode);

            if (forceEagerEvaluation) {
                Path path = outPath.doOperation();
                if (!path.toFile().exists()) {

                    if (skipMissingChannels) {
                        continue;
                    } else {
                        throw new AnchorIOException(
                                String.format("Append path: %s does not exist", path));
                    }
                }
            }

            out = new AppendPart(out, ni.getName(), 0, outPath, rasterReader);
        }

        return out;
    }

    public InputManager<NamedChnlsInputPart> getInput() {
        return input;
    }

    public void setInput(InputManager<NamedChnlsInputPart> input) {
        this.input = input;
    }

    public List<NamedBean<FilePathGenerator>> getListAppend() {
        return listAppend;
    }

    public void setListAppend(List<NamedBean<FilePathGenerator>> listAppend) {
        this.listAppend = listAppend;
    }

    public boolean isForceEagerEvaluation() {
        return forceEagerEvaluation;
    }

    public void setForceEagerEvaluation(boolean forceEagerEvaluation) {
        this.forceEagerEvaluation = forceEagerEvaluation;
    }

    public boolean isIgnoreFileNotFoundAppend() {
        return ignoreFileNotFoundAppend;
    }

    public void setIgnoreFileNotFoundAppend(boolean ignoreFileNotFoundAppend) {
        this.ignoreFileNotFoundAppend = ignoreFileNotFoundAppend;
    }

    public boolean isSkipMissingChannels() {
        return skipMissingChannels;
    }

    public void setSkipMissingChannels(boolean skipMissingChannels) {
        this.skipMissingChannels = skipMissingChannels;
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }

    public void setRasterReader(RasterReader rasterReader) {
        this.rasterReader = rasterReader;
    }
}
