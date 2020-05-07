package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.core.text.LanguageUtilities;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.io.generator.histogram.HistogramCSVGenerator;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.bean.grouped.BoundContextIntoSubfolder;
import org.anchoranalysis.plugin.image.task.grouped.GroupMap;

class GroupedHistogramWriter {

	private HistogramCSVGenerator generator = new HistogramCSVGenerator();
	
	public void writeHistogramToFile( Histogram hist, String outputName, String errorName, BoundIOContext context ) throws IOException {
		
		try {
			generator.setIterableElement(hist);
		} catch (SetOperationFailedException e) {
			Exception excComb =new OperationFailedException( String.format("Cannot write histogram for %s", errorName, e) );
			context.getErrorReporter().recordError(GroupMap.class, excComb);
		}
		
		context.getOutputManager().getWriterCheckIfAllowed().write(
			outputName,
			() -> generator
		);
	}
	
	public void writeAllGroupHistograms( GroupMap<?,Histogram> groupMap, BoundIOContext context ) throws IOException {
				
		if (!groupMap.keySet().isEmpty()) {
			context.getLogReporter().log(
				String.format(
					"Writing %s into %s",
					LanguageUtilities.prefixPluralize(groupMap.keySet().size(), "grouped histogram"),
					context.getOutputManager().getOutputFolderPath()
				)
			);
		}
		
		// We wish to create a new output-manager only once for each primary key, so we store them in a hashmap
		Map<String, BoundIOContext> mapOutputManagers = new HashMap<>();
		
		for( CombinedName group : groupMap.keySet() ) {
		
			BoundIOContext contextGroup = mapOutputManagers.computeIfAbsent(
				group.getPrimaryName(),
				primaryName -> new BoundContextIntoSubfolder(context, primaryName)
			);
			
			Histogram hist = groupMap.get(group);
			assert( hist!=null);
			writeHistogramToFileResolve(
				hist,
				group,
				contextGroup
			);
		}

	}
	
	public boolean isIgnoreZeros() {
		return generator.isIgnoreZeros();
	}

	public void setIgnoreZeros(boolean ignoreZeros) {
		generator.setIgnoreZeros(ignoreZeros);
	}
		
	// Resolves a new output folder before writing
	private void writeHistogramToFileResolve(
		Histogram hist,
		CombinedName outputIdentifier,
		BoundIOContext context
	) throws IOException {

		writeHistogramToFile(hist, outputIdentifier.getSecondaryName(), outputIdentifier.getCombinedName(), context);
	}
}
