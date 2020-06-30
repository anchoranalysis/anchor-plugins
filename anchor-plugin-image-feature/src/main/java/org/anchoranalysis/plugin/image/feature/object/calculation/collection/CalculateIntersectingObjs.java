package org.anchoranalysis.plugin.image.feature.object.calculation.collection;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Calculates the intersecting set of objects from a particular collection (represted by an id) and the object-mask in the params
 * @author owen
 *
 */
public class CalculateIntersectingObjs extends FeatureCalculation<ObjectCollection, FeatureInputSingleObject> {

	private String id;
	private ObjectCollection searchObjs;
		
	/**
	 * Constructor
	 * 
	 * @param id a unique ID that maps 1 to 1 to searchObjs (and is therefore sufficient to uniquely hashcode)
	 * @param searchObjs the objects corresponding to id
	 */
	public CalculateIntersectingObjs(String id, ObjectCollection searchObjs ) {
		super();
		this.id = id;
		this.searchObjs = searchObjs;
	}

	@Override
	protected ObjectCollection execute(FeatureInputSingleObject params) {

		ObjMaskCollectionRTree bboxRTree = new ObjMaskCollectionRTree( searchObjs );
		return bboxRTree.intersectsWith( params.getObjMask() );
	}

	@Override
	public boolean equals(Object obj) {
		 if(obj instanceof CalculateIntersectingObjs){
			 final CalculateIntersectingObjs other = (CalculateIntersectingObjs) obj;
		        return new EqualsBuilder()
		            .append(id, other.id)
		            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(id)
			.toHashCode();
	}

}
