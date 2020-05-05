package ch.ethz.biol.cell.imageprocessing.stack.provider;

/*
 * #%L
 * anchor-graph
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.util.Iterator;

import org.anchoranalysis.anchor.graph.AxisLimits;
import org.anchoranalysis.anchor.graph.GraphInstance;
import org.anchoranalysis.anchor.graph.bean.colorscheme.GraphColorScheme;
import org.anchoranalysis.anchor.graph.index.LinePlot;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;

class HistogramPlot {
	
	private HistogramPlot() throws InitException {}

	public static GraphInstance create( Iterator<HistogramItem> itr, AxisLimits domainLimits, AxisLimits rangeLimits ) throws CreateException {

		GraphColorScheme graphColorScheme = new GraphColorScheme();
		
		LinePlot<HistogramItem> plot = new LinePlot<>(
			"Histogram",
			new String[]{"Histogram"},
			(HistogramItem item, int yIndex) -> (double) item.getCount() 
		);		
		plot.getLabels().setX("Intensity");
		plot.getLabels().setY("Voxel Count");
		plot.setGraphColorScheme(graphColorScheme);
			
		return plot.create( itr, domainLimits, rangeLimits );
	}



}