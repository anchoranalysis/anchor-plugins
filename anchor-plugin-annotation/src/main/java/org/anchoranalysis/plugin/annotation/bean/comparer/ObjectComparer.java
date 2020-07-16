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


import java.nio.file.Path;

import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.annotation.io.wholeimage.findable.Found;
import org.anchoranalysis.annotation.io.wholeimage.findable.NotFound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.objects.ObjectCollectionReader;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.error.AnchorIOException;

import lombok.Getter;
import lombok.Setter;


/**
 * An object-collection to be used to compare against something
 * 
 * @author Owen Feehan
 *
 */
public class ObjectComparer extends Comparer {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private FilePathGenerator filePathGenerator;
	// END BEAN PROPERTIES
	
	@Override
	public Findable<ObjectCollection> createObjects(Path filePathSource, ImageDimensions dim, boolean debugMode) throws CreateException {
		
		try {
			Path objectsPath = filePathGenerator.outFilePath(filePathSource, debugMode);
			
			if (!objectsPath.toFile().exists()) {
				return new NotFound<>(objectsPath, "No objects exist");
			}
			
			return new Found<>(
				ObjectCollectionReader.createFromPath(objectsPath)
			);
			
		} catch (AnchorIOException | DeserializationFailedException e) {
			throw new CreateException(e);
		}
	}
}