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

import java.awt.Color;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.color.RGBColorBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.ColoredObjectCollection;

/**
 * Draws a colored representation (outline or filled) of a {@link ObjectMask} on a background
 *
 * @author Owen Feehan
 */
public class ColoredMask extends ColoredBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MaskProvider mask;

    @BeanField @Getter @Setter private RGBColorBean color = new RGBColorBean(Color.green);

    // END BEAN PROPERTIES

    @Override
    protected ColoredObjectCollection coloredObjectsToDraw(Dimensions backgroundDimensions)
            throws CreateException {
        try {
            ObjectMask maskAsObject = new ObjectMask(mask.get().binaryVoxels());
            return new ColoredObjectCollection(maskAsObject, color.toRGBColor());
        } catch (ProvisionFailedException e) {
            throw new CreateException(e);
        }
    }
}
