/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.provider.collection;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.OptionalOperationUnsupportedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.mpp.bean.provider.MarkCollectionProvider;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.spatial.scale.ScaleFactor;

public class ScaleXY extends MarkCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkCollectionProvider marks;

    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;
    // END BEAN PROPERTIES

    @Override
    public MarkCollection get() throws ProvisionFailedException {

        ScaleFactor scaleFactor;
        try {
            scaleFactor =
                    scaleCalculator.calculate(
                            Optional.empty(), getInitialization().image().getSuggestedResize());
        } catch (OperationFailedException e1) {
            throw new ProvisionFailedException(e1);
        }

        MarkCollection marksCreated = marks.get().deepCopy();
        try {
            marksCreated.scaleXY(scaleFactor);
        } catch (OptionalOperationUnsupportedException e) {
            throw new ProvisionFailedException(e);
        }
        return marksCreated;
    }
}
