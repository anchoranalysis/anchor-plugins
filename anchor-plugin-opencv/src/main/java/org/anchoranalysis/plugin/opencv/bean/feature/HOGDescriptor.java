/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.feature;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.feature.input.FeatureInputStack;

/**
 * Creates the entire HOG descriptor for an image.
 *
 * <p>The user is required to specify a size to which each image is resized so as to give a
 * constant-sized feature-descriptor for each image (and descriptors that are meaningful to compare
 * across images).
 *
 * @author Owen Feehan
 */
public class HOGDescriptor extends FeatureListProvider<FeatureInputStack> {

    // START BEAN PROPERTIES
    /** The input-image is rescaled to this width/height before calculating HOG descriptors */
    @BeanField @Getter @Setter private SizeXY resizeTo;

    /** Parameters used for calculating HOG */
    @BeanField @Getter @Setter private HOGParameters parameters = new HOGParameters();

    // END BEAN PROPERTIES

    @Override
    public FeatureList<FeatureInputStack> get() throws ProvisionFailedException {
        return FeatureListFactory.mapFromRange(
                0, parameters.sizeDescriptor(resizeTo.asExtent()), this::featureFor);
    }

    private HOGFeature featureFor(int index) {
        HOGFeature feature = new HOGFeature();
        feature.setResizeTo(resizeTo);
        feature.setParameters(parameters);
        feature.setIndex(index);
        return feature;
    }
}
