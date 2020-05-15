package ch.ethz.biol.cell.imageprocessing.stack.provider;

import java.util.Optional;

import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.ProviderNullableCreator;

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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.RGBObjMaskGenerator;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithPropertiesCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class StackProviderRGBFromObjMaskThreeColors extends StackProviderRGBFromObjMaskBase {

	private static final RGBColor COLOR_RED = new RGBColor(255,0,0);
	private static final RGBColor COLOR_GREEN = new RGBColor(0,255,0);
	private static final RGBColor COLOR_BLUE = new RGBColor(0,0,255);
		
	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private ObjMaskProvider objsRed;
	
	@BeanField @OptionalBean
	private ObjMaskProvider objsBlue;
	
	@BeanField @OptionalBean
	private ObjMaskProvider objsGreen;
	// END BEAN PROPERTIES

	@Override
	public void checkMisconfigured( BeanInstanceMap defaultInstances ) throws BeanMisconfiguredException {
		super.checkMisconfigured( defaultInstances );
		
		if (objsRed==null && objsBlue==null && objsGreen==null) {
			throw new BeanMisconfiguredException("Either objsRed or objsBlue or objsGreen must be non-null");
		}
	}

	@Override
	public Stack create() throws CreateException {
		
		ObjMaskCollection objs = new ObjMaskCollection();
		ColorList colors = new ColorList();
		
		addWithColor( objsRed, COLOR_RED, objs, colors );
		addWithColor( objsGreen, COLOR_GREEN, objs, colors );
		addWithColor( objsBlue, COLOR_BLUE, objs, colors );
		
		ObjMaskWriter objMaskWriter = createWriter();  
		
		RGBObjMaskGenerator generator = new RGBObjMaskGenerator(
			objMaskWriter,
			new ObjMaskWithPropertiesCollection(objs),
			maybeFlattenedBackground(),
			colors
		);
		
		try {
			return generator.generate();
		} catch (OutputWriteFailedException e) {
			throw new CreateException(e);
		}
	}

	private void addWithColor( ObjMaskProvider provider, RGBColor color, ObjMaskCollection objsOut, ColorList colorsInOut ) throws CreateException {
		Optional<ObjMaskCollection> providerObjs = ProviderNullableCreator.createOptional(provider);

		if (providerObjs.isPresent()) {
			ObjMaskCollection maybeFlattened = maybeFlatten(providerObjs.get()); 
			objsOut.addAll(maybeFlattened);
			colorsInOut.addMultiple( color, maybeFlattened.size() );
		}
	}
	
	public ObjMaskProvider getObjsRed() {
		return objsRed;
	}


	public void setObjsRed(ObjMaskProvider objsRed) {
		this.objsRed = objsRed;
	}


	public ObjMaskProvider getObjsBlue() {
		return objsBlue;
	}


	public void setObjsBlue(ObjMaskProvider objsBlue) {
		this.objsBlue = objsBlue;
	}


	public ObjMaskProvider getObjsGreen() {
		return objsGreen;
	}


	public void setObjsGreen(ObjMaskProvider objsGreen) {
		this.objsGreen = objsGreen;
	}
}
