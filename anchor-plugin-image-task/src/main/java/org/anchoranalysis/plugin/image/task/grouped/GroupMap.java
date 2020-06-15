package org.anchoranalysis.plugin.image.task.grouped;

import java.util.Map.Entry;
import java.util.Optional;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
 * #%L
 * anchor-image-experiment
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


import org.anchoranalysis.core.collection.TreeMapCreate;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.core.name.MultiName;
import org.anchoranalysis.core.name.SimpleName;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * Groups together many items in an aggregate-structure
 * 
 * @author Owen Feehan
 *
 * @param <S> individual-item type
 * @param <T> aggregate-item type (combines many individual types)
 */
public abstract class GroupMap<S,T> {

	private TreeMapCreate<MultiName,T,GetOperationFailedException> groupMap;
	private String nounT;

	/**
	 * 
	 * @param nounT a word to describe a single instance of T in user error messages
	 * @param createEmpty
	 */
	public GroupMap(String nounT, Operation<T,GetOperationFailedException> createEmpty ) {
		super();
		this.groupMap = new TreeMapCreate<>( createEmpty );
		this.nounT = nounT;
	}
	
	/**
	 * Adds an item with an identifier
	 * 
	 * @throws JobExecutionException
	 */
	public synchronized void add(
		Optional<String> groupIdentifier,
		String nonGroupIdentifier,
		S itemToAdd
	) throws JobExecutionException {

		MultiName identifier = createUniqueName(groupIdentifier, nonGroupIdentifier);
		
		try {
			T group = groupMap.getOrCreateNew(identifier);
			addTo(itemToAdd, group);
		} catch (GetOperationFailedException e) {
			throw new JobExecutionException(
				String.format("An error occurred creating a %s for: %s", nounT, identifier),
				e
			);
		} catch (OperationFailedException e) {
			throw new JobExecutionException(
				String.format("An error occurred combining the %s created for: %s", nounT, identifier),
				e
			);
		}
	}
	
	/**
	 * Outputs the "grouped" data to the file-system
	 * 
	 * @param chnlChecker channel checker
	 * @param context
	 * @throws IOException if something goes wrong, or if includeGroupName is FALSE, but more than one group-names exist
	 */
	public void outputGroupedData(
		ConsistentChnlChecker chnlChecker,
		BoundIOContext context
	) throws IOException {
		
		// We wish to create a new output-manager only once for each primary key, so we store them in a hashmap
		Map<String, BoundIOContext> mapOutputManagers = new HashMap<>();
		
		// The first-part of the CombinedName is the "group"
		// The second-part of the CombinedName is the histogram-channel name
		// We create a sub-folder for each first-part, and each channel into it
		
		// If there is one part-only, it is assumed that there is no group (for all items) and it is written without a subdirectory
		// If there are two parts, it is assumed that the first-part is a group-name (a seperate subdirectory) and the second-part is written without a subdirectory 
		for( Entry<MultiName,T> entry : groupMap.entrySet() ) {
		
			BoundIOContext contextGroup = mapOutputManagers.computeIfAbsent(
				entry.getKey().numParts()==1 ? "arbitrary_key" : entry.getKey().getPart(0),
				primaryName -> contextForGroup(primaryName, entry.getKey().numParts()==2, context)
			);

			writeGroupOutputInSubdirectory(
				entry.getKey().getPart( entry.getKey().numParts()-1 ),
				entry.getValue(),
				chnlChecker,
				contextGroup
			);
		}
	}
	
	private BoundIOContext contextForGroup(String primaryName, boolean includeGroupName, BoundIOContext context) {
		if (includeGroupName) {
			return context.subdirectory(primaryName);
		} else {
			return context;
		}
	}
	
	protected abstract void addTo( S ind, T agg ) throws OperationFailedException;
	
	protected abstract void writeGroupOutputInSubdirectory(
		String outputName,
		T agg,
		ConsistentChnlChecker chnlChecker,
		BoundIOContext context
	) throws IOException;
	
	private static MultiName createUniqueName(Optional<String> groupIdentifier, String nonGroupIdentifier) {
		return groupIdentifier.map( id->
			(MultiName) new CombinedName(id, nonGroupIdentifier)
		).orElseGet( ()->
			new SimpleName(nonGroupIdentifier)
		);
	}
}
