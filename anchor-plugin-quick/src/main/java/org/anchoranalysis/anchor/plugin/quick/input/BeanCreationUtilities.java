package org.anchoranalysis.anchor.plugin.quick.input;

/*-
 * #%L
 * anchor-plugin-quick
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.List;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.bean.input.Files;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.provider.file.RootedFileSet;
import org.anchoranalysis.image.io.bean.input.ImgChnlMapCreatorFactory;
import org.anchoranalysis.image.io.bean.input.ImgChnlMapEntry;
import org.anchoranalysis.image.io.bean.input.NamedChnls;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;

/** Helps in creating Beans through code */
public class BeanCreationUtilities {

	public static NamedChnls createNamedChnls(
			InputManager<FileInput> files,
			String mainChnlName,
			int mainChnlIndex,
			List<ImgChnlMapEntry> additionalChnls,
			RasterReader rasterReader
	) throws BeanMisconfiguredException {
		NamedChnls namedChnls = new NamedChnls();
		namedChnls.setImgChnlMapCreator(
			ImgChnlMapCreatorFactory.apply(
				mainChnlName,
				mainChnlIndex,
				additionalChnls
			)
		);
		namedChnls.setFileInput( files );
		namedChnls.setRasterReader(rasterReader);
		return namedChnls;
	}
	
	
	/**
	 * Creates a Files
	 * 
	 * @param rootName if non-empty a RootedFileProvier is used
	 * @param fileProvider fileProvider
	 * @param descriptiveNameFromFile descriptiveName
	 * @return
	 */
	public static InputManager<FileInput> createFiles( String rootName, FileProviderWithDirectory fileProvider, DescriptiveNameFromFile descriptiveNameFromFile ) {
		Files files = new Files();
		files.setFileProvider( createMaybeRootedFileProvider(rootName, fileProvider) );
		files.setDescriptiveNameFromFile(descriptiveNameFromFile);
		return files;
	}
	
	private static FileProvider createMaybeRootedFileProvider( String rootName, FileProviderWithDirectory fileProvider ) {
		if (rootName!=null && !rootName.isEmpty()) {
			return createRootedFileProvider(rootName, fileProvider);
		} else {
			return fileProvider;
		}
	}
	
	private static FileProvider createRootedFileProvider( String rootName, FileProviderWithDirectory fileProvider ) {
		RootedFileSet fileSet = new RootedFileSet();
		fileSet.setRootName(rootName);
		fileSet.setFileSet(fileProvider);
		fileSet.setDisableDebugMode(true);
		return fileSet;
	}
}
