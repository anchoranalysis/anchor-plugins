package ch.ethz.biol.cell.imageprocessing.objmask.provider.assignobjectstocfg;

/*
 * #%L
 * anchor-mpp
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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

public class ResolvedObjectList implements Iterable<ResolvedObject> {
	private List<ResolvedObject> delegate;
	
	public ResolvedObjectList() {
		delegate = new ArrayList<>();
	}
	
	public ResolvedObjectList(Stream<ResolvedObject> stream) {
		delegate = stream.collect(Collectors.toList());
	}

	public void add(ResolvedObject element) {
		delegate.add(element);
	}

	public Iterator<ResolvedObject> iterator() {
		return delegate.iterator();
	}
	
	public void addAllTo( ObjectCollection objectsOut ) {
		delegate.forEach(resolvedObject->
			objectsOut.add( resolvedObject.getObject() )
		);
	}

	public int size() {
		return delegate.size();
	}
	
	public ObjectCollection createObjects() {
		return ObjectCollectionFactory.mapFrom(delegate, ResolvedObject::getObject);
	}
}