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


import java.nio.file.Files;
import java.nio.file.Path;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.annotation.io.wholeimage.findable.Found;
import org.anchoranalysis.annotation.io.wholeimage.findable.NotFound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.mpp.io.cfg.CfgDeserializer;

public class CfgComparer extends Comparer {


	// START BEAN PROPERTIES
	@BeanField
	private FilePathGenerator filePathGenerator;
	// END BEAN PROPERTIES
	
	private RegionMembershipWithFlags rm = RegionMapSingleton.instance().membershipWithFlagsForIndex( GlobalRegionIdentifiers.SUBMARK_INSIDE );
	
	public CfgComparer() {
		super();
	}

	@Override
	public Findable<ObjectCollection> createObjs(Path filePathSource, ImageDim dim, boolean debugMode) throws CreateException {

		Path filePath;
		try {
			filePath = filePathGenerator.outFilePath(filePathSource, false);
		} catch (AnchorIOException e1) {
			throw new CreateException(e1);
		}
		
		if (!Files.exists( filePath )) {
			return new NotFound<>(filePath, "No cfg exists at path");	// There's nothing to annotate against
		}
		
		CfgDeserializer deserialized = new CfgDeserializer();
		Cfg cfg;
		try {
			cfg = deserialized.deserialize(filePath);
		} catch (DeserializationFailedException e) {
			throw new CreateException(e);
		}
		
		ObjectCollection mask = cfg.calcMask(
			dim,
			rm,
			BinaryValuesByte.getDefault(),
			null
		).collectionObjMask();
		return new Found<>(mask);
	}

	public FilePathGenerator getFilePathGenerator() {
		return filePathGenerator;
	}

	public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
		this.filePathGenerator = filePathGenerator;
	}

}
