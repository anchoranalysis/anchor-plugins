/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.grouper;

import org.anchoranalysis.io.input.bean.grouper.FromDerivePath;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.plugin.io.bean.path.derive.CollapseFileName;

/**
 * Splits an identifier into elements by the directory separator, and removes the final element.
 *
 * <p>If there is only one element (i.e. directory separator), it is left unchanged.
 *
 * @author Owen Feehan
 */
public class RemoveLastElement extends FromDerivePath {

    private static final CollapseFileName DELEGATE = new CollapseFileName();

    @Override
    protected DerivePath selectDerivePath() {
        return DELEGATE;
    }
}
