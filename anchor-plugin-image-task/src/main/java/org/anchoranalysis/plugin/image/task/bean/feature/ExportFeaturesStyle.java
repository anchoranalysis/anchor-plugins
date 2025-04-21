/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.feature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;

/**
 * Visual style for how features are exported.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
@AllArgsConstructor
public class ExportFeaturesStyle extends AnchorBean<ExportFeaturesStyle> {

    // START BEAN PROPERTIES
    /**
     * When true, columns containing all {@link Double#NaN} values are removed before outputting.
     *
     * <p>When false, all columns are included.
     */
    @BeanField @Getter @Setter private boolean removeNaNColumns = true;

    /**
     * When true, feature-values are shown as visually compressed as possible, including suppressing
     * decimal places.
     *
     * <p>When false, a fixed number of decimal places is shown for all feature-values.
     *
     * <p>e.g. when true, {@code 2} is shown instead of {@code 2.0000000000} or {@code 6.71} instead
     * of {@code 6.71000000000000}
     */
    @BeanField @Getter @Setter private boolean visuallyShortenDecimals = true;

    /**
     * When false, an image is reported as errored, if any exception is thrown during calculation.
     *
     * <p>When true, then a value of {@link Double#NaN} is returned, and a message is written to the
     * error-log.
     */
    @BeanField @Getter @Setter private boolean suppressErrors = false;
    // END BEAN PROPERTIES

    /**
     * Derives a {@link FeatureExporterContext} from the current style and given context.
     *
     * @param context the {@link InputOutputContext} to use for deriving the new context.
     * @return a new {@link FeatureExporterContext} instance.
     */
    public FeatureExporterContext deriveContext(InputOutputContext context) {
        return new FeatureExporterContext(
                context, removeNaNColumns, visuallyShortenDecimals, suppressErrors);
    }
}
