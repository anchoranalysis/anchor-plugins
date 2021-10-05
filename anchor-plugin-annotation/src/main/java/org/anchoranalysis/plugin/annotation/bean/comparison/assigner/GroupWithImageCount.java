/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.anchoranalysis.annotation.io.comparer.StatisticsToExport;
import org.anchoranalysis.plugin.annotation.counter.ImageCounterWithStatistics;

/**
 * A unique group that collects counts of how many images are annotated or not.
 *
 * <p>The payload to {@link #addAnnotatedImage} is ignored and irrelevant.
 *
 * @param <T> the payload-type for {@link #addAnnotatedImage}, unused, but perhaps convenient for
 *     type-consistency.
 * @author Owen Feehan
 */
@RequiredArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class GroupWithImageCount<T> implements ImageCounterWithStatistics<T> {

    // START REQUIRED ARGUMENTS
    /** The identifier of the group. */
    @Getter @EqualsAndHashCode.Include private final String identifier;
    // END REQUIRED ARGUMENTS

    /** The number of images <b>with</b> associated annotations. */
    @Getter private int countWithAnnotations = 0;

    /** The number of images <b>without</b> associated annotations. */
    @Getter private int countWithoutAnnotations = 0;

    @Override
    public void addUnannotatedImage() {
        countWithoutAnnotations++;
    }

    @Override
    public void addAnnotatedImage(T payload) {
        countWithAnnotations++;
    }

    public int numberImagesTotal() {
        return countWithAnnotations + countWithoutAnnotations;
    }

    public int numberImagesAnnotations() {
        return countWithAnnotations;
    }

    public double percentAnnotatedImages() {
        return ((double) numberImagesAnnotations() * 100) / numberImagesTotal();
    }

    @Override
    public StatisticsToExport comparison() {
        StatisticsToExport out = new StatisticsToExport();

        out.addString("name", identifier());
        out.addDouble("percentAnnotated", percentAnnotatedImages());
        out.addInt("countImages", numberImagesTotal());
        out.addInt("countImagesAnnotations", countWithAnnotations());
        out.addInt("countImagesWithoutAnnotations", countWithoutAnnotations());
        return out;
    }
}
