/*-
 * #%L
 * anchor-test-feature-plugins
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

package org.anchoranalysis.test.feature.plugins;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.xml.BeanXMLLoader;
import org.anchoranalysis.bean.xml.exception.BeanXMLException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.test.TestLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeaturesFromXmlFixture {

    public static <T extends FeatureInput> FeatureList<T> createFeatureList(
            String xmlPath, TestLoader loader) throws CreateException {
        Path pathStatic = loader.resolveTestPath(xmlPath);
        try {
            FeatureListProvider<T> provider = BeanXMLLoader.loadBean(pathStatic);
            FeatureList<T> features = provider.get();
            assertTrue(!features.isEmpty());
            return features;
        } catch (BeanXMLException | ProvisionFailedException e) {
            throw new CreateException(e);
        }
    }

    public static <T extends FeatureInput>
            List<NamedBean<FeatureListProvider<T>>> createNamedFeatureProviders(
                    String xmlPath, TestLoader loader) throws CreateException {
        Path pathStatic = loader.resolveTestPath(xmlPath);
        try {
            List<NamedBean<FeatureListProvider<T>>> list = BeanXMLLoader.loadBean(pathStatic);
            assertTrue(!list.isEmpty());
            return list;
        } catch (BeanXMLException e) {
            throw new CreateException(e);
        }
    }
}
