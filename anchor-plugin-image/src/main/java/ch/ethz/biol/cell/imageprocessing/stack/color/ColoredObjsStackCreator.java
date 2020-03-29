package ch.ethz.biol.cell.imageprocessing.stack.color;

import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;

/*-
 * #%L
 * anchor-plugin-image
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

import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGenerator;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithPropertiesCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.bean.objmask.writer.RGBSolidWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class ColoredObjsStackCreator {

	public static final ColorSetGenerator DEFAULT_COLOR_SET_GENERATOR = new ShuffleColorSetGenerator( new HSBColorSetGenerator() );
	
	/**
	 * 
	 * @param objs
	 * @param outline
	 * @param outlineWidth
	 * @param force2D
	 * @param background
	 * @param colors list of colors. If null, it is automatically generated.
	 * @return
	 * @throws CreateException
	 */
	public static Stack create(
		ObjMaskCollection objs,
		boolean outline,
		int outlineWidth,
		boolean force2D,
		DisplayStack background,
		ColorList colors
	) throws CreateException {
		
		try {
			
			ObjMaskWriter objMaskWriter = outline ? new RGBOutlineWriter(outlineWidth,force2D) : new RGBSolidWriter();  
			
			if (colors==null) {
				colors = DEFAULT_COLOR_SET_GENERATOR.genColors(objs.size());
			}
			
			RGBObjMaskGenerator generator = new RGBObjMaskGenerator(
				objMaskWriter,
				new ObjMaskWithPropertiesCollection(objs),
				background,
				colors
			);
			
			return generator.generate();
			
		} catch (OutputWriteFailedException | OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
}
