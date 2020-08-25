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

package org.anchoranalysis.plugin.mpp.bean.distance;

import org.anchoranalysis.mpp.bean.mark.MarkDistance;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.UnsupportedMarkTypeException;
import org.anchoranalysis.mpp.mark.conic.Circle;
import org.anchoranalysis.mpp.mark.conic.Ellipse;
import org.anchoranalysis.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.mpp.mark.conic.Sphere;

public class MarkDistanceRadii extends MarkDistance {

    @Override
    public boolean isCompatibleWith(Mark testMark) {

        if (testMark instanceof Circle) {
            return true;
        }

        if (testMark instanceof Ellipse) {
            return true;
        }

        if (testMark instanceof Ellipsoid) {
            return true;
        }

        return testMark instanceof Sphere;
    }

    @Override
    public double distance(Mark mark1, Mark mark2) throws UnsupportedMarkTypeException {

        if (mark1 instanceof Circle && mark2 instanceof Circle) {
            Circle mark1Cast = (Circle) mark1;
            Circle mark2Cast = (Circle) mark2;
            return Math.abs(mark1Cast.getRadius() - mark2Cast.getRadius());
        }

        if (mark1 instanceof Sphere && mark2 instanceof Sphere) {
            Sphere mark1Cast = (Sphere) mark1;
            Sphere mark2Cast = (Sphere) mark2;
            return Math.abs(mark1Cast.getRadius() - mark2Cast.getRadius());
        }

        if (mark1 instanceof Ellipse && mark2 instanceof Ellipse) {
            Ellipse mark1Cast = (Ellipse) mark1;
            Ellipse mark2Cast = (Ellipse) mark2;
            return Math.abs(mark1Cast.getRadii().distance(mark2Cast.getRadii()));
        }

        if (mark1 instanceof Ellipsoid && mark2 instanceof Ellipsoid) {
            Ellipsoid mark1Cast = (Ellipsoid) mark1;
            Ellipsoid mark2Cast = (Ellipsoid) mark2;
            return Math.abs(mark1Cast.getRadii().distance(mark2Cast.getRadii()));
        }

        throw new UnsupportedMarkTypeException("Marks not supported");
    }
}
