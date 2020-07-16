/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.session;

import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureInputCfg;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;

public class FeatureListFixtureMPP {

    private static TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    public static FeatureList<FeatureInputMark> mark() throws CreateException {
        return FeaturesFromXmlFixture.createFeatureList("markFeatureList.xml", loader);
    }

    public static FeatureList<FeatureInputCfg> cfg() throws CreateException {
        return FeaturesFromXmlFixture.createFeatureList("cfgFeatureList.xml", loader);
    }
}
