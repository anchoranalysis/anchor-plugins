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

package org.anchoranalysis.plugin.mpp.bean.mark.distance;

import org.anchoranalysis.anchor.mpp.bean.mark.MarkDistance;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.UnsupportedMarkTypeException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkCircle;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkSphere;

public class MarkDistanceRadii extends MarkDistance {

    @Override
    public boolean isCompatibleWith(Mark testMark) {

        if (testMark instanceof MarkCircle) {
            return true;
        }

        if (testMark instanceof MarkEllipse) {
            return true;
        }

        if (testMark instanceof MarkEllipsoid) {
            return true;
        }

        return testMark instanceof MarkSphere;
    }

    @Override
    public double distance(Mark mark1, Mark mark2) throws UnsupportedMarkTypeException {

        if (mark1 instanceof MarkCircle && mark2 instanceof MarkCircle) {
            MarkCircle mark1Cast = (MarkCircle) mark1;
            MarkCircle mark2Cast = (MarkCircle) mark2;
            return Math.abs(mark1Cast.getRadius() - mark2Cast.getRadius());
        }

        if (mark1 instanceof MarkSphere && mark2 instanceof MarkSphere) {
            MarkSphere mark1Cast = (MarkSphere) mark1;
            MarkSphere mark2Cast = (MarkSphere) mark2;
            return Math.abs(mark1Cast.getRadius() - mark2Cast.getRadius());
        }

        if (mark1 instanceof MarkEllipse && mark2 instanceof MarkEllipse) {
            MarkEllipse mark1Cast = (MarkEllipse) mark1;
            MarkEllipse mark2Cast = (MarkEllipse) mark2;
            return Math.abs(mark1Cast.getRadii().distance(mark2Cast.getRadii()));
        }

        if (mark1 instanceof MarkEllipsoid && mark2 instanceof MarkEllipsoid) {
            MarkEllipsoid mark1Cast = (MarkEllipsoid) mark1;
            MarkEllipsoid mark2Cast = (MarkEllipsoid) mark2;
            return Math.abs(mark1Cast.getRadii().distance(mark2Cast.getRadii()));
        }

        throw new UnsupportedMarkTypeException("Marks not supported");
    }
}
