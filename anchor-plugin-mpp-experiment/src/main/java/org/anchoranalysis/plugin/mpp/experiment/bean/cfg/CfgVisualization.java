/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.cfg;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.object.writer.Filled;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;

/**
 * Maybe writes two raster visualizations of the cfg, one solid, and one with an outline
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CfgVisualization {

    public static void write(
            Cfg cfg, BoundOutputManagerRouteErrors outputManager, DisplayStack backgroundStack)
            throws OperationFailedException {
        ColorIndex colorIndex =
                outputManager.getOutputWriteSettings().genDefaultColorIndex(cfg.size());

        WriterRouterErrors writeIfAllowed = outputManager.getWriterCheckIfAllowed();
        ColoredCfgWithDisplayStack cfgWithStack =
                new ColoredCfgWithDisplayStack(
                        new ColoredCfg(cfg, colorIndex, new IDGetterIter<Mark>()), backgroundStack);

        writeCfgGenerator(writeIfAllowed, "solid", new Filled(), cfgWithStack);
        writeCfgGenerator(writeIfAllowed, "outline", new Outline(), cfgWithStack);
    }

    private static void writeCfgGenerator(
            WriterRouterErrors writeIfAllowed,
            String outputName,
            DrawObject maskWriter,
            ColoredCfgWithDisplayStack cfgWithStack) {
        writeIfAllowed.write(
                outputName,
                () -> new CfgGenerator(maskWriter, cfgWithStack, new IDGetterIter<Overlay>()));
    }
}
