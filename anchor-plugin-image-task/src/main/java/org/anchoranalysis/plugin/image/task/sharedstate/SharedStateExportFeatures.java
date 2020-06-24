package org.anchoranalysis.plugin.image.task.sharedstate;

import java.io.IOException;
import java.util.Optional;

import org.anchoranalysis.feature.calc.results.ResultsVector;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.GroupedResultsVectorCollection;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;


/**
 * Shared-state for an export-features class
 * 
 * @author Owen Feehan
 */
public abstract class SharedStateExportFeatures {

	private final GroupedResultsVectorCollection groupedResults;
	
	public SharedStateExportFeatures(GroupedResultsVectorCollection groupedResults) {
		this.groupedResults = groupedResults;
	}
	
	public void addResultsFor( StringLabelsForCsvRow labels, ResultsVector results) {
		groupedResults.addResultsFor(labels, results);
	}

	/**
	 * Writes all the results that have been collected as a CSV file
	 * 
	 * @param <T> feature input-type
	 * @param featuresAggregate features that can be used for generating additional "aggregated" exports
	 * @param includeGroups iff TRUE a group-column is included in the CSV file and the group exports occur, otherwise not
	 * @param context io-context
	 * @throws AnchorIOException
	 */
	public <T extends FeatureInput> void writeGroupedResults(
		Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate,
		boolean includeGroups,
		BoundIOContext context
	) throws AnchorIOException {
		groupedResults.writeResultsForAllGroups(
			featuresAggregate,
			includeGroups,
			context
		);
	}

	public void closeAnyOpenIO() throws IOException {
		groupedResults.close();
	}
}
