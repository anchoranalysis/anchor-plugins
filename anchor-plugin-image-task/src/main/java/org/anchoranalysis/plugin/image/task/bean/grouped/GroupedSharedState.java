package org.anchoranalysis.plugin.image.task.bean.grouped;

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

import java.util.function.Function;

import org.anchoranalysis.plugin.image.grouped.ConsistentChnlChecker;
import org.anchoranalysis.plugin.image.grouped.GroupMap;

/**
 * Commonality between shared state for gouped export tasks
 * 
 * @author FEEHANO
 *
 * @param <S> individual-type
 * @param <T> aggregate-type
 */
public class GroupedSharedState<S,T> {

	private ConsistentChnlChecker chnlChecker = new ConsistentChnlChecker();
	
	private GroupMap<S,T> groupMap;
	
	private Function<ConsistentChnlChecker,GroupMap<S,T>> createGroupMap;
	
	public GroupedSharedState( Function<ConsistentChnlChecker,GroupMap<S,T>> createGroupMap ) {
		this.createGroupMap = createGroupMap;
	}
	
	public ConsistentChnlChecker getChnlChecker() {
		return chnlChecker;
	}

	public GroupMap<S, T> getGroupMap() {
		
		if (groupMap==null) {
			this.groupMap = createGroupMap.apply(chnlChecker);
		}
		
		return groupMap;
	}
}
