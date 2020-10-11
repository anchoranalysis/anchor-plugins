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
package org.anchoranalysis.plugin.quick.bean.input;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.files.provider.FilesProviderWithDirectory;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;

/**
 * A base for <i>quick</i> managers that make various assumptions about file-structure.
 *
 * <p>This provides a quicker means to specify certain projects.
 *
 * @author Owen Feehan
 * @param <T> input-type
 */
public abstract class QuickBase<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    /** If non-empty then a rooted filesystem is used with this root */
    @BeanField @AllowEmpty @Getter @Setter private String rootName = "";

    /** A path to the main channel of each file */
    @BeanField @Getter @Setter private FilesProviderWithDirectory filesProvider;

    @BeanField @Getter @Setter
    private DescriptiveNameFromFile descriptiveName = new LastFolders();

    /** If set, a CSV is read with two columns: the names of images and a */
    @BeanField @OptionalBean @Getter @Setter private MatchedAppendCsv filterFilesCsv;

    /**
     * A regular-expression applied to the image file-path that matches three groups. The first
     * group should correspond to the unique name of the top-level owner The second group should
     * correspond to the unique name of the dataset. The third group should correspond to the unique
     * name of the experiment.
     */
    @BeanField @OptionalBean @Getter @Setter private String regex;

    /** The raster-reader for reading the main file */
    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReader;

    /** The raster-reader for reading any appended files */
    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReaderAppend;
    // END BEAN PROPERTIES

    protected InputManager<FileInput> fileInputManager() throws BeanMisconfiguredException {
        return InputManagerFactory.createFiles(
                rootName, filesProvider, descriptiveName, regex, filterFilesCsv);
    }
}
