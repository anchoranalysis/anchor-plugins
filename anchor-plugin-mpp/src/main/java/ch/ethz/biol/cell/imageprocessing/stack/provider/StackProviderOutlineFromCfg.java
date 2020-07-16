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

package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;
import lombok.Getter;
import lombok.Setter;

public class StackProviderOutlineFromCfg extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private CfgProvider cfgProvider;

    @BeanField @Getter @Setter private StackProvider backgroundProvider;

    @BeanField @Getter @Setter private int outlineWidth = 1;

    @BeanField @Getter @Setter private ColorSetGenerator colorSetGenerator = new HSBColorSetGenerator();

    @BeanField @Getter @Setter private int regionID = 0;

    @BeanField @Getter @Setter private boolean mip = false;
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        Stack background = backgroundProvider.create();
        Cfg cfg = cfgProvider.create();

        try {
            ColorList colorList = colorSetGenerator.generateColors(cfg.size());

            ColoredCfg coloredCfg = new ColoredCfg(cfg, colorList);

            // Created colored cfg with display stack
            ColoredCfgWithDisplayStack coloredCfgWithDisplayStack;

            coloredCfgWithDisplayStack =
                    new ColoredCfgWithDisplayStack(coloredCfg, DisplayStack.create(background));

            CfgGenerator cfgGenerator =
                    new CfgGenerator(
                            new Outline(outlineWidth),
                            coloredCfgWithDisplayStack,
                            new IDGetterIter<Overlay>(),
                            RegionMapSingleton.instance().membershipWithFlagsForIndex(regionID));
            return cfgGenerator.generate();

        } catch (OperationFailedException | OutputWriteFailedException e1) {
            throw new CreateException(e1);
        }
    }
}
