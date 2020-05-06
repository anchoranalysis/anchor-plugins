package org.anchoranalysis.plugin.image.task.grouped;

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


import java.util.Set;
import java.util.stream.Collectors;

import org.anchoranalysis.core.cache.Operation;
import org.anchoranalysis.core.collection.TreeMapCreate;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.experiment.JobExecutionException;

/**
 * Groups together many items in an aggregate-structure
 * 
 * @author FEEHANO
 *
 * @param <S> individual-item type
 * @param <T> aggregate-item type (combines many individual types)
 */
public class GroupMap<S,T> {

	private TreeMapCreate<CombinedName,T,GetOperationFailedException> groupMap;
	private String nounT;
	private AddToAggregateItem<S, T> addTo;

	/**
	 * 
	 * @param nounT a word to describe a single instance of T in user error messages
	 * @param createEmpty
	 */
	public GroupMap(String nounT, Operation<T,GetOperationFailedException> createEmpty, AddToAggregateItem<S,T> addTo ) {
		super();
		this.groupMap = new TreeMapCreate<>( createEmpty );
		this.nounT = nounT;
		this.addTo = addTo;
	}
	
	/**
	 * Adds a T to a particular group
	 * 
	 * @throws JobExecutionException
	 */
	public synchronized void addToGroup( CombinedName groupIdentifier, S toAdd) throws JobExecutionException {

		try {
			T group = groupMap.getOrCreateNew(groupIdentifier);
			addTo.add(toAdd, group);
		} catch (GetOperationFailedException e) {
			throw new JobExecutionException(
				String.format("An error occurred creating a %s for group: %s", nounT, groupIdentifier),
				e
			);
		} catch (OperationFailedException e) {
			throw new JobExecutionException(
				String.format("An error occurred combinining the %s created for group: %s", nounT, groupIdentifier),
				e
			);
		}
	}
	
	public Set<CombinedName> keySet() {
		return groupMap.keySet();
	}
	
	public Set<String> keySetCombined() {
		return keySet()
				.stream()
				.map( a -> a.getCombinedName() )
				.collect(Collectors.toSet());
	}
	

	public T get(CombinedName key) {
		return groupMap.get(key);
	}
}
