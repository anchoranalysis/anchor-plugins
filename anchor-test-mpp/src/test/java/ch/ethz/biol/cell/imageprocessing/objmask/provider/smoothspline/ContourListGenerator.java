/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointList;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.io.generator.raster.RasterGenerator;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.generator.IterableObjectGenerator;
import org.anchoranalysis.io.generator.ObjectGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;

class ContourListGenerator extends RasterGenerator
        implements IterableObjectGenerator<List<Contour>, Stack> {

    private CfgGenerator delegate;

    private List<Contour> contourList;
    private DisplayStack stack;
    private ColorIndex colorIndex;

    public static ColorSetGenerator DEFAULT_COLOR_SET_GENERATOR =
            new ShuffleColorSetGenerator(new HSBColorSetGenerator());

    public ContourListGenerator(DisplayStack stack) {
        this(new Outline(1, true), null, stack);
    }

    public ContourListGenerator(DisplayStack stack, List<Contour> contours)
            throws SetOperationFailedException {
        this(stack);
        setIterableElement(contours);
    }

    public ContourListGenerator(DrawObject drawObject, ColorIndex colorIndex, DisplayStack stack) {
        this.stack = stack;
        delegate = new CfgGenerator(drawObject, new IDGetterIter<Overlay>());
        delegate.setManifestDescriptionFunction("contourRepresentationRGB");
        this.colorIndex = colorIndex;
    }

    @Override
    public boolean isRGB() {
        return delegate.isRGB();
    }

    @Override
    public Stack generate() throws OutputWriteFailedException {
        return delegate.generate();
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return delegate.createManifestDescription();
    }

    private static Cfg createCfgFromContourList(List<Contour> contourList) {

        Cfg cfg = new Cfg();

        for (Iterator<Contour> itr = contourList.iterator(); itr.hasNext(); ) {
            Contour contour = itr.next();

            cfg.add(createMarkForContour(contour, false));
        }
        return cfg;
    }

    @Override
    public void start() throws OutputWriteFailedException {
        delegate.start();
    }

    @Override
    public void end() throws OutputWriteFailedException {
        delegate.end();
    }

    @Override
    public List<Contour> getIterableElement() {
        return contourList;
    }

    @Override
    public void setIterableElement(List<Contour> element) throws SetOperationFailedException {

        this.contourList = element;

        try {
            ColoredCfg cfg =
                    new ColoredCfg(
                            createCfgFromContourList(contourList),
                            generateColors(contourList.size()),
                            new IDGetterIter<>());
            delegate.setIterableElement(new ColoredCfgWithDisplayStack(cfg, stack));

        } catch (OperationFailedException e) {
            throw new SetOperationFailedException(e);
        }
    }

    @Override
    public ObjectGenerator<Stack> getGenerator() {
        return delegate.getGenerator();
    }

    private ColorIndex generateColors(int size) throws OperationFailedException {
        if (colorIndex != null) {
            return colorIndex;
        } else {
            return DEFAULT_COLOR_SET_GENERATOR.generateColors(size);
        }
    }

    private static MarkPointList createMarkForContour(Contour c, boolean round) {
        return MarkPointListFactory.createMarkFromPoints3f(c.getPoints());
    }
}
