package org.anchoranalysis.plugin.annotation.bean.comparer;

/*
 * #%L
 * anchor-annotation
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.io.IOException;
import java.nio.file.Path;

import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.mark.MarkAnnotationReader;
import org.anchoranalysis.annotation.mark.MarkAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithPropertiesCollection;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;

public class AnnotationCfgComparer extends Comparer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FilePathGenerator filePathGenerator;
	// END BEAN PROPERTIES
	
	public AnnotationCfgComparer() {
		super();
	}
	
	@Override
	public ObjMaskCollection createObjs(Path filePathSource, ImageDim dim, boolean debugMode) throws CreateException {

		Path filePath;
		try {
			filePath = filePathGenerator.outFilePath(filePathSource, false);
		} catch (IOException e1) {
			throw new CreateException(e1);
		}
		
		MarkAnnotationReader annotationReader = new MarkAnnotationReader(false);
		MarkAnnotation annotation;
		try {
			annotation = annotationReader.read( filePath );
		} catch (IOException e) {
			throw new CreateException(e);
		}
		
		if (annotation==null) {
			return null;
		}
		
		if (!annotation.isAccepted()) {
			return null;
		}
		
		ObjMaskWithPropertiesCollection omwp = annotation.getCfg().calcMask(
			dim,
			annotation.getRegionMap().membershipWithFlagsForIndex(annotation.getRegionID()),
			BinaryValuesByte.getDefault(),
			null
		); 
		return omwp.collectionObjMask();
	}

	public FilePathGenerator getFilePathGenerator() {
		return filePathGenerator;
	}

	public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
		this.filePathGenerator = filePathGenerator;
	}



}
