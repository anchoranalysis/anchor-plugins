package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.bean.BeanInstanceMap;

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
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.io.generator.raster.objmask.rgb.RGBObjMaskGenerator;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithPropertiesCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.bean.objmask.writer.RGBSolidWriter;
import org.anchoranalysis.io.output.OutputWriteFailedException;

import ch.ethz.biol.cell.imageprocessing.io.objmask.ObjMaskWriter;

public class StackProviderRGBFromObjMaskThreeColors extends StackProviderWithBackground {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	// START BEAN PROPERTIES
	@BeanField @Optional
	private ObjMaskProvider objMaskProviderRed;
	
	@BeanField @Optional
	private ObjMaskProvider objMaskProviderBlue;
	
	@BeanField @Optional
	private ObjMaskProvider objMaskProviderGreen;
		
	@BeanField
	private boolean outline = false;
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private boolean force2D = false;
	// END BEAN PROPERTIES

	@Override
	public void checkMisconfigured( BeanInstanceMap defaultInstances ) throws BeanMisconfiguredException {
		super.checkMisconfigured( defaultInstances );
		
		if (objMaskProviderRed==null && objMaskProviderBlue==null && objMaskProviderGreen==null) {
			throw new BeanMisconfiguredException("Either objMaskProviderRed or objMaskProviderBlue or objMaskProviderGreen must be non-null");
		}
	}

	
	private void addColor( ObjMaskProvider provider, RGBColor color, ObjMaskCollection objsInOut, ColorList colorsInOut ) throws CreateException {
		
		if (provider!=null) {
			ObjMaskCollection objsToAdd = provider.create();
			objsInOut.addAll(objsToAdd);
			colorsInOut.addMultiple( color, objsToAdd.size() );
		}
	}
	
	@Override
	public Stack create() throws CreateException {
	
		DisplayStack background = backgroundStack(!force2D);
				
		
		ObjMaskCollection objs = new ObjMaskCollection();
		ColorList colors = new ColorList();
		
		addColor( objMaskProviderRed, new RGBColor(255,0,0), objs, colors );
		addColor( objMaskProviderGreen, new RGBColor(0,255,0), objs, colors );
		addColor( objMaskProviderBlue, new RGBColor(0,0,255), objs, colors );
		
		ObjMaskWriter objMaskWriter = outline ? new RGBOutlineWriter(outlineWidth,force2D) : new RGBSolidWriter();  
		
		RGBObjMaskGenerator generator = new RGBObjMaskGenerator( objMaskWriter, new ObjMaskWithPropertiesCollection(objs), background, colors);
		
		try {
			return generator.generate();
		} catch (OutputWriteFailedException e) {
			throw new CreateException(e);
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

	public ObjMaskProvider getObjMaskProviderRed() {
		return objMaskProviderRed;
	}

	public void setObjMaskProviderRed(ObjMaskProvider objMaskProviderRed) {
		this.objMaskProviderRed = objMaskProviderRed;
	}

	public ObjMaskProvider getObjMaskProviderBlue() {
		return objMaskProviderBlue;
	}

	public void setObjMaskProviderBlue(ObjMaskProvider objMaskProviderBlue) {
		this.objMaskProviderBlue = objMaskProviderBlue;
	}

	public ObjMaskProvider getObjMaskProviderGreen() {
		return objMaskProviderGreen;
	}

	public void setObjMaskProviderGreen(ObjMaskProvider objMaskProviderGreen) {
		this.objMaskProviderGreen = objMaskProviderGreen;
	}


}
