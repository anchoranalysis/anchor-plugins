/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.experiment.task.NoSharedState;

/**
 * Abstract base class for image labellers that produce binary outcomes (positive or negative).
 *
 * @author Owen Feehan
 */
public abstract class BinaryOutcomeImageLabeller extends ImageLabeller<NoSharedState> {

    /** String representation for a positive classification. */
    private static final String POSITIVE = "positive";

    /** String representation for a negative classification. */
    private static final String NEGATIVE = "negative";

    @Override
    public NoSharedState initialize(Path pathForBinding) throws InitializeException {
        return NoSharedState.INSTANCE;
    }

    @Override
    public Set<String> allLabels(NoSharedState sharedState) {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(POSITIVE, NEGATIVE)));
    }

    /**
     * Converts a boolean classification result to its string representation.
     *
     * @param positive true if the classification is positive, false if negative
     * @return {@link #POSITIVE} if true, {@link #NEGATIVE} if false
     */
    protected static String classificationString(boolean positive) {
        return positive ? POSITIVE : NEGATIVE;
    }
}
