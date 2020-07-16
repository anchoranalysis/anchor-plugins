/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.summarizer;

import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;

/**
 * Collects summary data about a set of input files that will eventually be outputted to the user
 */
public class SummarizerAggregate<T> extends Summarizer<T> {

    private static final String BULLET_POINT = "-> ";

    // START BEAN PROPERTIES
    @BeanField private List<Summarizer<T>> list;

    /** Iff TRUE no bullet is added for the very first-item in the list */
    @BeanField private boolean avoidBulletOnFirst = false;
    // END BEAN PROPERTIES

    public void add(T element) throws OperationFailedException {

        for (Summarizer<T> summarizer : list) {
            summarizer.add(element);
        }
    }

    @Override
    public String describe() throws OperationFailedException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {

            if (i != 0) {
                sb.append(System.lineSeparator());
            }

            if (!(avoidBulletOnFirst && i == 0)) {
                sb.append(BULLET_POINT);
            }

            Summarizer<T> element = list.get(i);
            sb.append(element.describe());
        }

        return sb.toString();
    }

    public List<Summarizer<T>> getList() {
        return list;
    }

    public void setList(List<Summarizer<T>> list) {
        this.list = list;
    }

    public boolean isAvoidBulletOnFirst() {
        return avoidBulletOnFirst;
    }

    public void setAvoidBulletOnFirst(boolean avoidBulletOnFirst) {
        this.avoidBulletOnFirst = avoidBulletOnFirst;
    }
}
