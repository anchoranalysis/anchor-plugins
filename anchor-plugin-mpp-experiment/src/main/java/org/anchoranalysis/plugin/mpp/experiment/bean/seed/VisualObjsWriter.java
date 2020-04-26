package org.anchoranalysis.plugin.mpp.experiment.bean.seed;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import java.util.function.Function;

import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGenerator;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithPropertiesCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.collection.IterableGeneratorWriter;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;

/**
 * Outputs a ObjMaskCollection in different ways 
 *
 */
class VisualObjsWriter {

	private BoundOutputManagerRouteErrors outputManager;
	private Chnl chnl;
	private ObjMaskCollection objs;
			
	public VisualObjsWriter(BoundOutputManagerRouteErrors outputManager, Chnl chnl, ObjMaskCollection objs) {
		super();
		this.outputManager = outputManager;
		this.chnl = chnl;
		this.objs = objs;
	}
	
	public void writeAsSubfolder( String outputName, WriterRouterErrors writer, Function<Chnl, IterableGenerator<ObjMask>> funcGeneratorIterable ) {
		// Write out the results as a subfolder
		IterableGeneratorWriter.writeSubfolder(
			outputManager,
			outputName,
			outputName,
			() -> funcGeneratorIterable.apply(chnl),
			objs.asList(),
			true
		);
	}
	
	public void writeAsImage( String outputName, WriterRouterErrors writer, ObjMaskWriter objMaskWriter) {
		writer.write(
			outputName,
			() -> maskGenerator(objMaskWriter)
		);
	}

	private RGBObjMaskGenerator maskGenerator(ObjMaskWriter objMaskWriter) throws OutputWriteFailedException {
		try {
			return new RGBObjMaskGenerator(
				objMaskWriter,
				new ObjMaskWithPropertiesCollection(objs),
				DisplayStack.create(chnl),
				outputManager.getOutputWriteSettings().genDefaultColorIndex(objs.size())
			);
		} catch (OperationFailedException | CreateException e) {
			throw new OutputWriteFailedException(e);
		}			
	}
}
