package ch.ethz.biol.cell.imageprocessing.io.generator.imagestack;

/*
 * #%L
 * anchor-plugin-ij
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


import ij.ImageStack;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.io.generator.raster.RasterGenerator;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.rgb.RGBStack;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class RasterConverterGenerator extends ImageStackGenerator {

	private RasterGenerator rasterGenerator;
	
	public RasterConverterGenerator(RasterGenerator rasterGenerator) {
		super();
		this.rasterGenerator = rasterGenerator;
	}

	@Override
	public ImageStack generate() throws OutputWriteFailedException {
		
		Stack stack = rasterGenerator.generate();
		
		try {
			return IJWrap.createColorProcessorStack( new RGBStack(stack) );
		} catch (CreateException e) {
			throw new OutputWriteFailedException(e);
		}
	}

	@Override
	public ManifestDescription createManifestDescription() {
		return rasterGenerator.createManifestDescription();
	}

}
