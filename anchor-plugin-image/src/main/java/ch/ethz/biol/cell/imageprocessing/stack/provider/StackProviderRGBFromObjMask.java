package ch.ethz.biol.cell.imageprocessing.stack.provider;



import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;

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
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ColorListProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;

import ch.ethz.biol.cell.imageprocessing.stack.color.ColoredObjsStackCreator;
import lombok.Getter;
import lombok.Setter;

public class StackProviderRGBFromObjMask extends StackProviderRGBFromObjMaskBase {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objs;
	
	@BeanField @OptionalBean @Getter @Setter
	private ColorListProvider colorListProvider;	// If null, uses the colorListGenerator below

	// Fallback generator if colorListProvider is null
	@BeanField @Getter @Setter
	private ColorSetGenerator colorSetGenerator = ColoredObjsStackCreator.DEFAULT_COLOR_SET_GENERATOR;
	// END BEAN PROPERTIES
	
	@Override
	public Stack create() throws CreateException {
				
		ObjectCollection objsCollection = objs.create();
		return createStack(
			objsCollection,
			colors(objsCollection.size())
		);
	}
		
	private ColorList colors( int size ) throws CreateException {
		Optional<ColorList> colorList = OptionalFactory.create(colorListProvider);
		
		if (!colorList.isPresent()) {
			try {
				return colorSetGenerator.genColors( size );
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			} 
		}
		
		return colorList.get();
	}
}
