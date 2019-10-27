package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.io.generator.histogram.HistogramCSVGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.grouped.GroupMap;

class GroupedHistogramWriter {

	private HistogramCSVGenerator generator = new HistogramCSVGenerator();
	
	public void writeHistogramToFile( Histogram hist, String outputName, String errorName, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter ) {
		
		try {
			generator.setIterableElement(hist);
		} catch (SetOperationFailedException e) {
			Exception excComb =new OperationFailedException( String.format("Cannot write histogram for %s", errorName, e) );
			logErrorReporter.getErrorReporter().recordError(GroupMap.class, excComb);
		}
		
		outputManager.getWriterCheckIfAllowed().write(
			outputName,
			() -> generator
		);
	}
	
	public void writeAllGroupHistograms( GroupMap<?,Histogram> groupMap, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter ) {
		for( CombinedName group : groupMap.keySet() ) {
		
			Histogram hist = groupMap.get(group);
			assert( hist!=null);
			writeHistogramToFileResolve( hist, group, generator, outputManager, logErrorReporter );
		}		
	}

	public boolean isIgnoreZeros() {
		return generator.isIgnoreZeros();
	}

	public void setIgnoreZeros(boolean ignoreZeros) {
		generator.setIgnoreZeros(ignoreZeros);
	}
		
	// Resolves a new output folder before writing
	private void writeHistogramToFileResolve( Histogram hist, CombinedName outputIdentifier, HistogramCSVGenerator generator, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter ) {
		BoundOutputManagerRouteErrors outputManagerGroup = outputManager.resolveFolder(
			outputIdentifier.getPrimaryName()
		);
		
		logErrorReporter.getLogReporter().log(
			String.format("Writing histogram for %s into %s", outputIdentifier.getPrimaryName(), outputManagerGroup.getOutputFolderPath() )
		);
				
		writeHistogramToFile(hist, outputIdentifier.getSecondaryName(), outputIdentifier.getCombinedName(), outputManagerGroup, logErrorReporter);
	}
}
