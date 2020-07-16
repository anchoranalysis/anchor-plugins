/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.overlay.id.IDGetterOverlayID;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.color.HashedColorSet;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.PeriodicSubfolderReporter;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class TiffTimeSeriesReporter extends PeriodicSubfolderReporter<CfgNRG> {

    // START Bean Properties
    @BeanField private int numColors = 20;
    // END Bean Properties

    private ColorIndex colorIndex;

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {

        try {
            colorIndex =
                    new HashedColorSet(
                            new ShuffleColorSetGenerator(new HSBColorSetGenerator()), numColors);
        } catch (OperationFailedException e1) {
            throw new ReporterException(e1);
        }

        IterableGenerator<ColoredCfgWithDisplayStack> iterableRaster =
                new CfgGenerator(new Outline(), new IDGetterOverlayID());

        // This no longer needs to be combined, it's a legacy of when a HTML reporter was attached
        //   cleaning up woould be nice
        IterableCombinedListGenerator<CfgNRG> iterableCombined =
                new IterableCombinedListGenerator<>(
                        new IterableGeneratorBridge<>(
                                iterableRaster,
                                sourceObject -> addColor(sourceObject.getCfg(), initParams)));

        try {
            init(iterableCombined);
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }

        super.reportBegin(initParams);
    }

    private ColoredCfgWithDisplayStack addColor(
            Cfg cfg, OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) {
        DisplayStack stack = initParams.getInitContext().getDualStack().getBgStack();
        ColoredCfg coloredCfg = new ColoredCfg(cfg, colorIndex, new IDGetterIter<Mark>());
        return new ColoredCfgWithDisplayStack(coloredCfg, stack);
    }

    @Override
    protected Optional<CfgNRG> generateIterableElement(Reporting<CfgNRGPixelized> reporting)
            throws ReporterException {
        return Optional.of(reporting.getCfgNRGAfter().getCfgNRG());
    }

    public int getNumColors() {
        return numColors;
    }

    public void setNumColors(int numColors) {
        this.numColors = numColors;
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }
}
