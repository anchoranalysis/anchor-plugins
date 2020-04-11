package org.anchoranalysis.plugin.image.feature.obj.intersecting;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationCastParams;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Calculates the intersecting set of objects from a particular collection (represted by an id) and the object-mask in the params
 * @author owen
 *
 */
public class CalculateIntersectingObjs extends CachedCalculationCastParams<ObjMaskCollection, FeatureObjMaskParams> {

	private String id;
	private ObjMaskCollection searchObjs;
		
	/**
	 * Constructor
	 * 
	 * @param id a unique ID that maps 1 to 1 to searchObjs (and is therefore sufficient to uniquely hashcode)
	 * @param searchObjs the objects corresponding to id
	 */
	public CalculateIntersectingObjs(String id, ObjMaskCollection searchObjs ) {
		super();
		this.id = id;
		this.searchObjs = searchObjs;
	}

	@Override
	protected ObjMaskCollection execute(FeatureObjMaskParams params) throws ExecuteException {

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

	@Override
	public CachedCalculation<ObjMaskCollection> duplicate() {
		return new CalculateIntersectingObjs(id, searchObjs);
	}

}
