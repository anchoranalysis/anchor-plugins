/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.anchor.plugin.quick.bean.input;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.AppendStack;
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorReplace;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.io.bean.input.chnl.NamedChnlsAppend;
import org.anchoranalysis.plugin.io.bean.input.chnl.NamedChnlsBase;

/**
 * A particular type of NamedChnls which allows easier input of data, making several assumptions.
 *
 * <p>This is a convenient helper class to avoid a more complicated structure
 */
public class NamedChnlsQuick extends NamedChnlsBase {

    // START BEAN PROPERTIES
    /** A path to the main channel of each file */
    @BeanField @Getter @Setter private FileProviderWithDirectory fileProvider;

    @BeanField @Getter @Setter
    private DescriptiveNameFromFile descriptiveNameFromFile = new LastFolders();

    /**
     * This needs to be set if there is at least one adjacentChnl
     *
     * <p>This should be a regex (with a single group that is replaced) that is searched for in the
     * path returned by fileProvider This only needs to be set if at least one adjacentChnl is
     * specified
     */
    @BeanField @AllowEmpty @Getter @Setter private String regexAdjacent = "";

    /**
     * This should be a regex that is searched for in the path returned by fileProvider and returns
     * two groups, the first
     */

    /**
     * This needs to be set if there is at least one appendChnl
     *
     * <p>A regular-expression applied to the image file-path that matches three groups. The first
     * group should correspond to top-level folder for the project The second group should
     * correspond to the unique name of the dataset. The third group should correspond to the unique
     * name of the experiment.
     */
    @BeanField @AllowEmpty @Getter @Setter private String regexAppend = "";

    /** The name of the channel provided by the rasters in file Provider */
    @BeanField @Getter @Setter private String mainChnlName;

    /** Index of the mainChnl */
    @BeanField @Getter @Setter private int mainChnlIndex = 0;

    /** Additional channels other than the main one, which are located in the main raster file */
    @BeanField @Getter @Setter private List<ImgChnlMapEntry> additionalChnls = new ArrayList<>();

    /** Channels that are located in a separate raster file adjacent to the main raster file */
    @BeanField @Getter @Setter private List<AdjacentFile> adjacentChnls = new ArrayList<>();

    /**
     * Channels that are located in a separate raster file somewhere else in the project's structure
     */
    @BeanField @Getter @Setter private List<AppendStack> appendChnls = new ArrayList<>();

    /** If non-empty then a rooted file-system is used with this root */
    @BeanField @AllowEmpty @Getter @Setter private String rootName = "";

    /** If set, a CSV is read with two columns: the names of images and a */
    @BeanField @OptionalBean @Getter @Setter private MatchedAppendCsv filterFilesCsv;

    /** The raster-reader to use for opening the main image */
    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    /** The raster-reader to use for opening any appended-channels */
    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReaderAppend;

    /** The raster-reader to use for opening any adjacent-channels */
    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReaderAdjacent;
    // END BEAN PROPERTIES

    private InputManager<NamedChnlsInputPart> append;

    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;

        checkChnls(adjacentChnls, regexAdjacent, "adjacentChnl");
        checkChnls(appendChnls, regexAppend, "appendChnl");
    }

    /**
     * Checks that if a list has an item, then a string must be non-empty
     *
     * @param list
     * @param mustBeNonEmpty
     * @param errorText
     * @throws BeanMisconfiguredException
     */
    private static void checkChnls(List<?> list, String mustBeNonEmpty, String errorText)
            throws BeanMisconfiguredException {
        if (!list.isEmpty() && mustBeNonEmpty.isEmpty()) {
            throw new BeanMisconfiguredException(
                    String.format(
                            "If an %s is specified, regex must also be specified", errorText));
        }
    }

    @Override
    public List<NamedChnlsInputPart> inputObjects(InputManagerParams params)
            throws AnchorIOException {
        createAppendedChnlsIfNecessary();
        return append.inputObjects(params);
    }

    private void createAppendedChnlsIfNecessary() throws AnchorIOException {
        if (this.append == null) {
            try {
                this.append = createAppendedChnls();
                append.checkMisconfigured(defaultInstances);
            } catch (BeanMisconfiguredException e) {
                throw new AnchorIOException("defaultInstances bean is misconfigured", e);
            }
        }
    }

    private InputManager<NamedChnlsInputPart> createAppendedChnls()
            throws BeanMisconfiguredException {

        InputManager<FileInput> files =
                InputManagerFactory.createFiles(
                        rootName,
                        fileProvider,
                        descriptiveNameFromFile,
                        regexAppend,
                        filterFilesCsv);

        InputManager<NamedChnlsInputPart> chnls =
                NamedChnlsCreator.create(
                        files, mainChnlName, mainChnlIndex, additionalChnls, rasterReader);

        chnls = appendChnls(chnls, createFilePathGeneratorsAdjacent(), rasterReaderAdjacent);

        chnls = appendChnls(chnls, createFilePathGeneratorsAppend(), rasterReaderAppend);

        return chnls;
    }

    private static NamedChnlsAppend appendChnls(
            InputManager<NamedChnlsInputPart> input,
            List<NamedBean<FilePathGenerator>> filePathGenerators,
            RasterReader rasterReader) {
        NamedChnlsAppend append = new NamedChnlsAppend();
        append.setIgnoreFileNotFoundAppend(false);
        append.setForceEagerEvaluation(false);
        append.setInput(input);
        append.setListAppend(filePathGenerators);
        append.setRasterReader(rasterReader);
        return append;
    }

    private List<NamedBean<FilePathGenerator>> createFilePathGeneratorsAdjacent() {
        return FunctionalList.mapToList(adjacentChnls, this::convertAdjacentFile);
    }

    private List<NamedBean<FilePathGenerator>> createFilePathGeneratorsAppend()
            throws BeanMisconfiguredException {

        List<NamedBean<FilePathGenerator>> out = new ArrayList<>();

        for (AppendStack stack : appendChnls) {
            try {
                out.add(convertAppendStack(stack));
            } catch (BeanMisconfiguredException e) {
                throw new BeanMisconfiguredException(
                        String.format(
                                "Cannot create file-path-generator for %s and regex %s",
                                stack.getName(), regexAppend),
                        e);
            }
        }

        return out;
    }

    private NamedBean<FilePathGenerator> convertAdjacentFile(AdjacentFile file) {

        FilePathGeneratorReplace fpg = new FilePathGeneratorReplace();
        fpg.setRegex(regexAdjacent);
        fpg.setReplacement(file.getReplacement());
        return new NamedBean<>(file.getName(), fpg);
    }

    private NamedBean<FilePathGenerator> convertAppendStack(AppendStack stack)
            throws BeanMisconfiguredException {
        return stack.createFilePathGenerator(rootName, regexAppend);
    }
}
