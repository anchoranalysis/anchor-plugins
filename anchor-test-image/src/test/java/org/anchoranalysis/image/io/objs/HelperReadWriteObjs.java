package org.anchoranalysis.image.io.objs;



/*-
 * #%L
 * anchor-test-image
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

import java.nio.file.Path;

import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.io.outputmanager.TempBoundOutputManager;
import org.anchoranalysis.plugin.io.outputmanager.TestReaderWriterUtilities;

class HelperReadWriteObjs {

	public static final String TEMPORARY_FOLDER_OUT = "debug/objs";
		
	public static IterableGenerator<ObjMaskCollection> generator( boolean hdf5, boolean compression ) {
		if (hdf5) {
			return new GeneratorHDF5(compression);
		} else {
			return new GeneratorTIFFDirectory();
		}
	}
	
	public static void writeObjs( ObjMaskCollection objs, Path path, IterableGenerator<ObjMaskCollection> generator ) throws SetOperationFailedException {
		generator.setIterableElement(objs);
		
		BoundOutputManagerRouteErrors outputManager = TempBoundOutputManager.outputManagerFor(path);
		
		outputManager.getWriterAlwaysAllowed().write(
			"objs",
			() -> generator.getGenerator()
		);
	}
	
	public static ObjMaskCollection readObjs(Path path) throws DeserializationFailedException {
		
		TestReaderWriterUtilities.ensureRasterReader();

		return ObjMaskCollectionReader.createFromPath(path);
	}
	
}
