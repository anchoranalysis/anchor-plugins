/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package ch.ethz.biol.cell.mpp.nrg.feature.session;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.image.extent.Dimensions;

public class CfgFixture {

    private final MarkFixture markFixture;

    public CfgFixture(Dimensions dimensions) {
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
