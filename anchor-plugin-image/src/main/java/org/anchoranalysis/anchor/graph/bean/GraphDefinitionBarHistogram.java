package org.anchoranalysis.anchor.graph.bean;

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
import org.anchoranalysis.anchor.graph.HistogramItem;
import org.anchoranalysis.anchor.graph.bean.colorscheme.GraphColorScheme;
import org.anchoranalysis.anchor.graph.index.LinePlot;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;

public class GraphDefinitionBarHistogram extends GraphDefinition<HistogramItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8653709262024962761L;
	
	// START BEAN PROPERITES
	@BeanField
	private GraphColorScheme graphColorScheme = new GraphColorScheme();
	// END BEAN PROPERTIES
	
	private LinePlot<HistogramItem> delegate;
	
	public GraphDefinitionBarHistogram() throws InitException {

		delegate = new LinePlot<>(
			getTitle(),
			new String[]{"Histogram"},
			(HistogramItem item, int yIndex) -> (double) item.getCount() 
		);		
		delegate.getLabels().setX("Intensity");
		delegate.getLabels().setY("Voxel Count");
		delegate.setGraphColorScheme(graphColorScheme);
	}

	@Override
	public GraphInstance create( Iterator<HistogramItem> itr, AxisLimits domainLimits, AxisLimits rangeLimits ) throws CreateException {
		return delegate.create( itr, domainLimits, rangeLimits );
	}

	@Override
	public String getTitle() {
		return "Histogram";
	}

	@Override
	public boolean isItemAccepted(HistogramItem item) {
		return true;
	}

	@Override
	public String getShortTitle() {
		return getTitle();
	}

	public GraphColorScheme getGraphColorScheme() {
		return graphColorScheme;
	}

	public void setGraphColorScheme(GraphColorScheme graphColorScheme) {
		this.graphColorScheme = graphColorScheme;
	}


}
