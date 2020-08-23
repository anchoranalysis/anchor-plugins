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

package org.anchoranalysis.plugin.mpp.bean.provider.single;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkCollectionProvider;
import org.anchoranalysis.anchor.mpp.bean.provider.SingleMarkProvider;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkCollection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;

/**
 * Retrieves a mark from a cfg, assuming there is only one mark in a cfg, otherwise throwing an
 * exception
 * 
 * @author Owen Feehan
 *
 */
public class RetrieveSingleMarkOnly extends SingleMarkProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkCollectionProvider cfgProvider;
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> create() throws CreateException {
        MarkCollection cfg = cfgProvider.create();

        if (cfg.size() == 0) {
            throw new CreateException("Cfg is empty. It must have exactly one item");
        }

        if (cfg.size() > 1) {
            throw new CreateException("Cfg has multiple marks. It must have exactly one item");
        }

        return Optional.of(cfg.get(0));
    }
}
