package ch.ethz.biol.cell.imageprocessing.stack.provider;



/*
 * #%L
 * anchor-plugin-image
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ColorListProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;

import ch.ethz.biol.cell.imageprocessing.stack.color.ColoredObjsStackCreator;

public class StackProviderRGBFromObjMask extends StackProviderWithBackground {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objMaskProvider;
		
	@BeanField
	private boolean outline = false;
	
	@BeanField @Optional
	private ColorListProvider colorListProvider;	// If null, uses the colorListGenerator below
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private boolean force2D = false;

	// Fallback generator if colorListProvider is null
	@BeanField
	private ColorSetGenerator colorSetGenerator = ColoredObjsStackCreator.DEFAULT_COLOR_SET_GENERATOR;
	// END BEAN PROPERTIES
	
	@Override
	public Stack create() throws CreateException {
				
		ObjMaskCollection objs = objMaskProvider.create();
		
		return ColoredObjsStackCreator.create(
			maybeFlatten(objs),
			outline,
			outlineWidth,
			force2D,
			backgroundStack(!force2D),
			colors(objs.size())
		);
	}
	
	private ObjMaskCollection maybeFlatten( ObjMaskCollection objs ) {
		if (force2D) {
			return objs.flattenZ();
		} else {
			return objs;
		}
	}
		
	private ColorList colors( int size ) throws CreateException {
		ColorList colorList;
		
		if (colorListProvider!=null) {
			colorList = colorListProvider.create();
		} else {
			try {
				colorList = colorSetGenerator.genColors( size );
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			} 
		}
		
		assert( colorList.size()==size );
		return colorList;
	}

	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}

	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
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

	public ColorListProvider getColorListProvider() {
		return colorListProvider;
	}

	public void setColorListProvider(ColorListProvider colorListProvider) {
		this.colorListProvider = colorListProvider;
	}

	public ColorSetGenerator getColorSetGenerator() {
		return colorSetGenerator;
	}

	public void setColorSetGenerator(ColorSetGenerator colorSetGenerator) {
		this.colorSetGenerator = colorSetGenerator;
	}
}
