/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature.report;

import java.io.IOException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.manifest.Manifest;
import org.anchoranalysis.io.manifest.finder.FinderSerializedObject;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.plugin.io.manifest.DeserializedManifest;

public class NumberMarksFromManifest extends ReportFeatureForManifest {

    @Override
    public String featureDescription(DeserializedManifest param, Logger logger)
            throws OperationFailedException {

        FinderSerializedObject<MarkCollection> finder =
                new FinderSerializedObject<>(
                        "marks", logger);

        Manifest manifest = param.get();

        if (!finder.doFind(manifest)) {
            throw new OperationFailedException("Cannot find marks in manifest");
        }

        try {
            return Integer.toString(finder.get().size());
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public String title() {
        return "marksSize";
    }
}
