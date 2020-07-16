/* (C)2020 */
package org.anchoranalysis.test.feature.plugins;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.xml.BeanXmlLoader;
import org.anchoranalysis.bean.xml.error.BeanXmlException;
import org.anchoranalysis.core.error.CreateException;
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
            FeatureListProvider<T> provider = BeanXmlLoader.loadBean(pathStatic);
            FeatureList<T> features = provider.create();
            assertTrue(!features.isEmpty());
            return features;
        } catch (BeanXmlException e) {
            throw new CreateException(e);
        }
    }

    public static <T extends FeatureInput>
            List<NamedBean<FeatureListProvider<T>>> createNamedFeatureProviders(
                    String xmlPath, TestLoader loader) throws CreateException {
        Path pathStatic = loader.resolveTestPath(xmlPath);
        try {
            List<NamedBean<FeatureListProvider<T>>> list = BeanXmlLoader.loadBean(pathStatic);
            assertTrue(!list.isEmpty());
            return list;
        } catch (BeanXmlException e) {
            throw new CreateException(e);
        }
    }
}
