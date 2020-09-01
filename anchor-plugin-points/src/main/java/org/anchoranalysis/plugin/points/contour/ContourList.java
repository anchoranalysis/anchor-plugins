/*-
 * #%L
 * anchor-plugin-points
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

package org.anchoranalysis.plugin.points.contour;

import java.util.ArrayList;
import java.util.Set;
import org.anchoranalysis.image.contour.Contour;

public class ContourList extends ArrayList<Contour> {

    /** */
    private static final long serialVersionUID = 7003021223351541824L;

    // Create one large contour with all the points
    public Contour createCombinedContour() {
        Contour out = new Contour();
        for (Contour contour : this) {
            contour.getPoints().stream().forEach(out.getPoints()::add);
        }
        return out;
    }

    public int numberFoundFrom(Set<Contour> set) {
        int cnt = 0;
        for (Contour contour : this) {
            if (set.contains(contour)) {
                cnt++;
            }
        }
        return cnt;
    }

    public Contour getFirst() {
        return get(0);
    }

    public Contour getLast() {
        return get(size() - 1);
    }

    public boolean firstConnectedTo(ContourList other) {
        return getFirst().connectedTo(other.getLast());
    }

    public boolean lastConnectedTo(ContourList other) {
        return getLast().connectedTo(other.getFirst());
    }

    @Override
    public String toString() {
        String newLine = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder();
        sb.append("--" + newLine);
        for (int i = 0; i < size(); i++) {
            Contour contour = get(i);
            sb.append(contour.summaryStr() + newLine);
        }

        sb.append("--" + newLine);
        return sb.toString();
    }
}
