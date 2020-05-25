package org.anchoranalysis.plugin.io.bean.rasterreader;

/*-
 * #%L
 * anchor-plugin-io
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

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.plugin.io.xml.AnchorMetadataXml;

public class ReadVoxelExtentXml extends RasterReader {

	// START BEAN PROPERTIES
	@BeanField
	private RasterReader rasterReader;
	
	@BeanField
	private boolean acceptNoResolution = true;
	// END BEAN PROPERTIES

	
	/**
	 * Looks for a metadata file describing the resolution
	 * 
	 * Given an existing image filepath, the filePath.xml is checked
	 * e.g. given /somePath/rasterReader.tif it will look for /somePath/RasterRader.tif.xml  
	 * 
	 * @param filepath the filepath of the image
	 * @param acceptNoResolution
	 * @return the scene res if the metadata file exists and was parsed. null otherwise.
	 * @throws RasterIOException
	 */
	public static Optional<ImageRes> readMetadata( Path filepath, boolean acceptNoResolution ) throws RasterIOException {
		
		// How we try to open the metadata
		Optional<ImageRes> res = null;
		File fileMeta = new File( filepath.toString() + ".xml" );
		  
		if( fileMeta.exists() ) {
			res = Optional.of(
				AnchorMetadataXml.readResolutionXml( fileMeta )
			);
		} else {
			if (acceptNoResolution==false) {
				throw new RasterIOException( String.format("Resolution metadata is required for '%s'", filepath) );
			}
		}
		return res;
	}
		
	private static void replaceResIfNotNull( ImageDim dims, Optional<ImageRes> res ) {
		if (res.isPresent()) {
			dims.setRes(res.get());
		}
	}
	
	@Override
	public OpenedRaster openFile(Path filepath) throws RasterIOException {
		
		OpenedRaster delegate = rasterReader.openFile(filepath);
		
		Optional<ImageRes> sr = readMetadata(filepath, acceptNoResolution);
		
		return new OpenedRasterAlterDimensions(
			delegate,
			dim -> replaceResIfNotNull(dim,sr)
		);
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}

	public boolean isAcceptNoResolution() {
		return acceptNoResolution;
	}

	public void setAcceptNoResolution(boolean acceptNoResolution) {
		this.acceptNoResolution = acceptNoResolution;
	}

}
