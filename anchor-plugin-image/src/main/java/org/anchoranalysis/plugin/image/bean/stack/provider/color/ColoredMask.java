/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.bean.stack.provider.color;

import lombok.Getter;
import lombok.Setter;
import java.awt.Color;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.plugin.image.object.ColoredObjectCollection;

/**
 * Draws a colored representation (outline or filled) of a {@link Mask} on a background
 * 
 * @author Owen Feehan
 *
 */
public class ColoredMask extends ColoredBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MaskProvider mask;
    
    @BeanField @Getter @Setter private RGBColorBean color = new RGBColorBean(Color.green);
    // END BEAN PROPERTIES

    @Override
    protected ColoredObjectCollection coloredObjectsToDraw(Dimensions backgroundDimensions) throws CreateException {
        ObjectMask maskAsObject = new ObjectMask(mask.create());
        return new ColoredObjectCollection(maskAsObject, color.rgbColor());
    }
}