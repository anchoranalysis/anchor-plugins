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
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.FilePathBaseAppendToManager;
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManagerBase;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.io.bean.input.stack.Stacks;

/** A quicker for of multi-input manager that makes various assumptions */
public class MultiInputManagerQuick extends MultiInputManagerBase {

    // START BEAN PROPERTIES
    /** If non-empty then a rooted file-system is used with this root */
    @BeanField @AllowEmpty @Getter @Setter private String rootName = "";

    @BeanField @Getter @Setter private FileProviderWithDirectory fileProvider;

    @BeanField @Getter @Setter
    private DescriptiveNameFromFile descriptiveNameFromFile = new LastFolders();

    @BeanField @Getter @Setter private String inputName;

    /*** Additional entities that are appended to the multi-input */
    @BeanField @Getter @Setter
    private List<FilePathBaseAppendToManager> listAppend = new ArrayList<>();

    /**
     * A regular-expression applied to the image file-path that matches three groups. The first
     * group should correspond to the unique name of the top-level owner The second group should
     * correspond to the unique name of the dataset. The third group should correspond to the unique
     * name of the experiment.
     */
    @BeanField @OptionalBean @Getter @Setter private String regex;

    /**
     * Additional channels other than the main one, which are located in the main raster file
     *
     * <p>If this list has at least one, then we treat the main raster file not as a stack, but
     * break it into separate channels that are each presented as a separate stack to the MultiInput
     */
    @BeanField @Getter @Setter private List<ImgChnlMapEntry> additionalChnls = new ArrayList<>();

    /** If set, a CSV is read with two columns: the names of images and a */
    @BeanField @OptionalBean @Getter @Setter private MatchedAppendCsv filterFilesCsv;

    /**
     * If true, a raster-stack is treated as a single-channel, even if multiple exist (and no
     * additionalChnl is set)
     */
    @BeanField @Getter @Setter private boolean stackAsChnl = false;

    /**
     * If either stackAsChnl==TRUE or we have specified additionalChnls this indicated which channel
     * to use from the stack
     */
    @BeanField @Getter @Setter private int chnlIndex = 0;

    /** The raster-reader for reading the main file */
    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    /** The raster-reader for reading any appended files */
    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReaderAppend;
    // END BEAN PROPERTIES

    private MultiInputManager inputManager;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        this.inputManager = createMulti();
        inputManager.checkMisconfigured(defaultInstances);

        if (!additionalChnls.isEmpty() && regex == null) {
            throw new BeanMisconfiguredException(
                    "If there is at least one additionalChnl then regex must be set");
        }
    }

    private MultiInputManager createMulti() throws BeanMisconfiguredException {
        MultiInputManager input = new MultiInputManager();
        input.setInputName(inputName);
        input.setInput(createStacks());
        input.setRasterReader(rasterReaderAppend);

        // Add all the various types of items that can be appended
        for (FilePathBaseAppendToManager append : listAppend) {
            append.addToManager(input, rootName, regex);
        }

        return input;
    }

    private InputManager<? extends ProvidesStackInput> createStacks()
            throws BeanMisconfiguredException {
        InputManager<FileInput> files =
                InputManagerFactory.createFiles(
                        rootName, fileProvider, descriptiveNameFromFile, regex, filterFilesCsv);

        if (stackAsChnl || !additionalChnls.isEmpty()) {
            // Then we treat the main raster as comprising of multiple independent channels
            //  and each are presented separately to the MultiInput as stacks
            //
            // Channel 0 always takes the inputName
            // The other channels are defined by the contents of the ImgChnlMapEntry
            return NamedChnlsCreator.create(
                    files, inputName, chnlIndex, additionalChnls, rasterReader);

        } else {
            // Normal mode, where we simply wrap the FileProvider in a Stacks
            Stacks stacks = new Stacks(files);
            stacks.setRasterReader(rasterReader);
            return stacks;
        }
    }

    @Override
    public List<MultiInput> inputObjects(InputManagerParams params) throws AnchorIOException {
        return inputManager.inputObjects(params);
    }
}
