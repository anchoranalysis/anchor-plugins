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

import java.util.Optional;

import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.rgb.RGBStack;
import org.anchoranalysis.io.generator.IterableObjectGenerator;
import org.anchoranalysis.io.generator.ObjectGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Converts a RasterGenerator into a ImageStackGenerator
 * @author Owen Feehan
 *
 * @param <T> iteration-type
 */
public class RasterConverterIterableGenerator<T> extends ImageStackGenerator implements IterableObjectGenerator<T, ImageStack> {

	private IterableObjectGenerator<T,Stack> rasterGenerator;
	
	public RasterConverterIterableGenerator(IterableObjectGenerator<T,Stack> rasterGenerator) {
		super();
		this.rasterGenerator = rasterGenerator;
	}

	@Override
	public ImageStack generate() throws OutputWriteFailedException {
		Stack stack = rasterGenerator.getGenerator().generate();
		return IJWrap.createColorProcessorStack( new RGBStack(stack) );
	}

	@Override
	public Optional<ManifestDescription> createManifestDescription() {
		return rasterGenerator.getGenerator().createManifestDescription();
	}

	@Override
	public void start() throws OutputWriteFailedException {
		rasterGenerator.start();
	}

	@Override
	public void end() throws OutputWriteFailedException {
		rasterGenerator.end();
	}

	@Override
	public T getIterableElement() {
		return rasterGenerator.getIterableElement();
	}

	@Override
	public void setIterableElement(T element) throws SetOperationFailedException {
		this.rasterGenerator.setIterableElement(element);
	}

	@Override
	public ObjectGenerator<ImageStack> getGenerator() {
		return this;
	}

}
