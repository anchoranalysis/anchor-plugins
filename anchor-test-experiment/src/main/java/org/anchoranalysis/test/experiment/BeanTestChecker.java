/*-
 * #%L
 * anchor-test-experiment
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

package org.anchoranalysis.test.experiment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.initializable.InitializableBean;
import org.anchoranalysis.bean.initializable.parameters.BeanInitialization;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.core.log.Logger;

/**
 * Checks to see if a bean has been misconfigured, when created manually in tests (thereby skipping
 * the usual checks during the BeanXML loading process).
 *
 * <p>Additionally wraps the exceptions thrown in {@link AnchorFriendlyRuntimeException} to make
 * tests more readable, rather than having too many different checked exception types in the test
 * code, for non-operational failures.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanTestChecker {

    /**
     * Checks if a bean has all necessary items, throwing a run-time exception if it does not.
     *
     * @param <T> bean-type
     * @param bean bean to check
     * @return the bean that was checked.
     */
    public static <T extends AnchorBean<?>> T check(T bean) {
        try {
            bean.checkMisconfigured(new BeanInstanceMap());
        } catch (BeanMisconfiguredException e) {
            throw new AnchorFriendlyRuntimeException(e);
        }
        return bean;
    }

    /**
     * Checks if a bean has all necessary items as with {@link #check} and also initializes the
     * bean.
     *
     * @param <T> bean-type
     * @param <P> initialization-parameters-type accepted by the bean
     * @param bean the bean to check and initialize.
     * @param initialization initialization-parameters.
     * @param logger the logger.
     * @return the bean that was checked and initialized.
     */
    public static <T extends InitializableBean<?, P>, P extends BeanInitialization> T checkAndInit(
            T bean, P initialization, Logger logger) {
        check(bean);
        try {
            bean.initializeRecursive(initialization, logger);
        } catch (InitializeException e) {
            throw new AnchorFriendlyRuntimeException(e);
        }
        return bean;
    }
}
