/* (C)2020 */
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

public class StackProviderOutlineFromCfg extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField private CfgProvider cfgProvider;

    @BeanField private StackProvider backgroundProvider;

    @BeanField private int outlineWidth = 1;

    @BeanField private ColorSetGenerator colorSetGenerator = new HSBColorSetGenerator();

    @BeanField private int regionID = 0;

    @BeanField private boolean mip = false;
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

    public int getOutlineWidth() {
        return outlineWidth;
    }

    public void setOutlineWidth(int outlineWidth) {
        this.outlineWidth = outlineWidth;
    }

    public ColorSetGenerator getColorSetGenerator() {
        return colorSetGenerator;
    }

    public void setColorSetGenerator(ColorSetGenerator colorSetGenerator) {
        this.colorSetGenerator = colorSetGenerator;
    }

    public CfgProvider getCfgProvider() {
        return cfgProvider;
    }

    public void setCfgProvider(CfgProvider cfgProvider) {
        this.cfgProvider = cfgProvider;
    }

    public StackProvider getBackgroundProvider() {
        return backgroundProvider;
    }

    public void setBackgroundProvider(StackProvider backgroundProvider) {
        this.backgroundProvider = backgroundProvider;
    }

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }

    public boolean isMip() {
        return mip;
    }

    public void setMip(boolean mip) {
        this.mip = mip;
    }
}
