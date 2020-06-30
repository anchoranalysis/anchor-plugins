package ch.ethz.biol.cell.imageprocessing.stack.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.bean.objmask.writer.RGBSolidWriter;

import ch.ethz.biol.cell.imageprocessing.stack.color.ColoredObjsStackCreator;

public abstract class StackProviderRGBFromObjMaskBase extends StackProviderWithBackground {

	// START BEAN PROPERTIES
	@BeanField
	private boolean outline = false;
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private boolean force2D = false;
	// END BEAN PROPERTIES
	
	protected Stack createStack( ObjectCollection objs, ColorList colors) throws CreateException {
		return ColoredObjsStackCreator.create(
			maybeFlatten(objs),
			outline,
			outlineWidth,
			force2D,
			maybeFlattenedBackground(),
			colors
		);
	}
	
	protected DisplayStack maybeFlattenedBackground() throws CreateException {
		return backgroundStack(!force2D);
	}
	
	protected ObjectCollection maybeFlatten( ObjectCollection objs ) {
		if (force2D) {
			return objs.stream().map(ObjectMask::flattenZ);
		} else {
			return objs;
		}
	}
	
	protected ObjMaskWriter createWriter() {
		if (outline) {
			return new RGBOutlineWriter(outlineWidth,force2D);
		} else {
			return new RGBSolidWriter();
		}
	}
	
	public boolean isOutline() {
		return outline;
	}

	public void setOutline(boolean outline) {
		this.outline = outline;
	}


	public int getOutlineWidth() {
		return outlineWidth;
	}

	public void setOutlineWidth(int outlineWidth) {
		this.outlineWidth = outlineWidth;
	}

	public boolean isForce2D() {
		return force2D;
	}

	public void setForce2D(boolean force2d) {
		force2D = force2d;
	}
}
