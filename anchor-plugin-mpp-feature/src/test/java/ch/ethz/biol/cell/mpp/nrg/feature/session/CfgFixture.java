/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.session;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.image.extent.ImageDimensions;

public class CfgFixture {

    private final MarkFixture markFixture;

    public CfgFixture(ImageDimensions dimensions) {
        this.markFixture = new MarkFixture(dimensions);
    }

    public Cfg createCfg1() {
        Cfg cfg = new Cfg();
        cfg.add(markFixture.createEllipsoid1());
        cfg.add(markFixture.createEllipsoid2());
        return cfg;
    }

    public Cfg createCfg2() {
        Cfg cfg = new Cfg();
        cfg.add(markFixture.createEllipsoid1());
        cfg.add(markFixture.createEllipsoid2());
        cfg.add(markFixture.createEllipsoid3());
        return cfg;
    }

    public Cfg createCfg3() {
        Cfg cfg = new Cfg();
        cfg.add(markFixture.createEllipsoid2());
        cfg.add(markFixture.createEllipsoid3());
        return cfg;
    }

    public Cfg createCfgSingle() {
        Cfg cfg = new Cfg();
        cfg.add(markFixture.createEllipsoid2());
        return cfg;
    }
}
