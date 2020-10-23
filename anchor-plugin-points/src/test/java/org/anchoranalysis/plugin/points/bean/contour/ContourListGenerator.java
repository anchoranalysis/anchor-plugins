/*-
 * #%L
 * anchor-test-mpp
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

package org.anchoranalysis.plugin.points.bean.contour;

import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.bean.shared.color.scheme.ColorScheme;
import org.anchoranalysis.bean.shared.color.scheme.HSB;
import org.anchoranalysis.bean.shared.color.scheme.Shuffle;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.getter.IdentifyByIteration;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.image.io.generator.raster.RasterGeneratorDelegateToRaster;
import org.anchoranalysis.mpp.io.marks.ColoredMarksWithDisplayStack;
import org.anchoranalysis.mpp.io.marks.generator.MarksGenerator;
import org.anchoranalysis.mpp.mark.ColoredMarks;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.points.PointList;
import org.anchoranalysis.mpp.mark.points.PointListFactory;
import org.anchoranalysis.overlay.Overlay;
import org.anchoranalysis.overlay.bean.DrawObject;
import org.anchoranalysis.spatial.Contour;

class ContourListGenerator
        extends RasterGeneratorDelegateToRaster<ColoredMarksWithDisplayStack, List<Contour>> {

    private final DisplayStack background;
    private final ColorIndex colorIndex;

    public static ColorScheme DEFAULT_COLOR_SET_GENERATOR = new Shuffle(new HSB());

    public ContourListGenerator(DisplayStack background) {
        this(new Outline(1, false), null, background);
    }

    public ContourListGenerator(
            DrawObject drawObject, ColorIndex colorIndex, DisplayStack background) {
        super(createDelegate(drawObject));
        this.background = background;
        this.colorIndex = colorIndex;
    }

    @Override
    protected ColoredMarksWithDisplayStack convertBeforeAssign(List<Contour> element)
            throws OperationFailedException {
        ColoredMarks marks =
                new ColoredMarks(
                        createMarksFromContourList(element),
                        generateColors(element.size()),
                        new IdentifyByIteration<>());
        return new ColoredMarksWithDisplayStack(marks, background);
    }

    @Override
    protected Stack convertBeforeTransform(Stack stack) {
        return stack;
    }

    private ColorIndex generateColors(int size) throws OperationFailedException {
        if (colorIndex != null) {
            return colorIndex;
        } else {
            return DEFAULT_COLOR_SET_GENERATOR.createList(size);
        }
    }

    private static PointList createMarkForContour(Contour c, boolean round) {
        return PointListFactory.createMarkFromPoints3f(c.getPoints());
    }

    private static MarkCollection createMarksFromContourList(List<Contour> contourList) {

        MarkCollection marks = new MarkCollection();

        for (Iterator<Contour> itr = contourList.iterator(); itr.hasNext(); ) {
            marks.add(createMarkForContour(itr.next(), false));
        }
        return marks;
    }

    private static MarksGenerator createDelegate(DrawObject drawObject) {
        MarksGenerator delegate = new MarksGenerator(drawObject, new IdentifyByIteration<Overlay>());
        delegate.setManifestDescriptionFunction("contourRepresentationRGB");
        return delegate;
    }
}