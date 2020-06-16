package org.anchoranalysis.plugin.image.task.bean.obj;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.ChnlMaskedWithObjGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.ObjAsBinaryChnlGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGenerator;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.generator.collection.IterableGeneratorWriter;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

class ObjMaskSgmnTaskOutputter {
	
	private ObjMaskSgmnTaskOutputter() {
		
	}
	
	public static void writeOriginal( BoundOutputManagerRouteErrors outputManager, Channel chnl, String outputName ) {
		outputManager.getWriterCheckIfAllowed().write(
			outputName,
			() -> new ChnlGenerator(chnl,"original")
		);
	}

	public static void writeMaskOutputs( ObjectCollection objs, Channel chnl, BoundOutputManagerRouteErrors outputManager ) {
		writeMaskChnlAsSubfolder(objs, chnl, outputManager);
		writeMasksAsSubfolder(objs, chnl, outputManager);
		writeOutline(objs, chnl, outputManager);		
	}
	
	private static void writeMaskChnlAsSubfolder( ObjectCollection objs, Channel chnl, BoundOutputManagerRouteErrors outputManager ) {
		// Write out the results as a subfolder
		IterableGeneratorWriter.writeSubfolder(
			outputManager,
			"maskChnl",
			"maskChnl",
			() -> new ChnlMaskedWithObjGenerator(chnl),
			objs.asList(),
			true
		);
	}
	
	private static void writeMasksAsSubfolder( ObjectCollection objs, Channel chnl, BoundOutputManagerRouteErrors outputManager ) {
		// Write out the results as a subfolder
		IterableGeneratorWriter.writeSubfolder(
			outputManager,
			"mask",
			"mask",
			() -> new ObjAsBinaryChnlGenerator(255, chnl.getDimensions().getRes() ),
			objs.asList(),
			true
		);	
	}
	
	private static void writeOutline( ObjectCollection objs, Channel chnl, BoundOutputManagerRouteErrors outputManager ) {
		outputManager.getWriterCheckIfAllowed().write(
			"outline",
			() -> {
				try {
					return new RGBObjMaskGenerator(
						new RGBOutlineWriter(),
						new ObjectCollectionWithProperties(objs),
						DisplayStack.create(chnl),
						outputManager.getOutputWriteSettings().genDefaultColorIndex(objs.size())
					);
				} catch (CreateException | OperationFailedException e) {
					throw new OutputWriteFailedException(e);
				}
			}
		);
	}
}
