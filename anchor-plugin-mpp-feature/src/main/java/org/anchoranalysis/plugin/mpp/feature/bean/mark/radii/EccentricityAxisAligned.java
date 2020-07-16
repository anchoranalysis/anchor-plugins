/*-
 * #%L
 * anchor-plugin-mpp-feature
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
/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.image.orientation.Orientation;

// Calculates the eccentricity of the ellipse
// If it's an ellipsoid it calculates the Meridional Eccentricity i.e. the eccentricity
//    of the ellipse that cuts across the plane formed by the longest and shortest axes
public class EccentricityAxisAligned extends FeatureMarkEccentricity {

    @Override
    protected double calcEccentricityForEllipsoid(double[] radii, Orientation orientation) {
        double e0 = calcEccentricity(radii[1], radii[0]);
        double e1 = calcEccentricity(radii[2], radii[1]);
        double e2 = calcEccentricity(radii[2], radii[0]);
        return (e0 + e1 + e2) / 3;
    }
}
