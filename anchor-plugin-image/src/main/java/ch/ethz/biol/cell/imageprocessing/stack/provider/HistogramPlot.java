/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import java.util.Iterator;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.plot.AxisLimits;
import org.anchoranalysis.anchor.plot.GraphInstance;
import org.anchoranalysis.anchor.plot.bean.colorscheme.GraphColorScheme;
import org.anchoranalysis.anchor.plot.index.LinePlot;
import org.anchoranalysis.core.error.CreateException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class HistogramPlot {

    public static GraphInstance create(
            Iterator<HistogramItem> itr,
            Optional<AxisLimits> domainLimits,
            Optional<AxisLimits> rangeLimits)
            throws CreateException {

        GraphColorScheme graphColorScheme = new GraphColorScheme();

        LinePlot<HistogramItem> plot =
                new LinePlot<>(
                        "Histogram",
                        new String[] {"Histogram"},
                        (HistogramItem item, int yIndex) -> (double) item.getCount());
        plot.getLabels().setX("Intensity");
        plot.getLabels().setY("Voxel Count");
        plot.setGraphColorScheme(graphColorScheme);

        return plot.create(itr, domainLimits, rangeLimits);
    }
}
