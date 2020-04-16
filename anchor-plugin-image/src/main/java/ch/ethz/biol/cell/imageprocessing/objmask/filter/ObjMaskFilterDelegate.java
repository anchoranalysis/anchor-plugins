package ch.ethz.biol.cell.imageprocessing.objmask.filter;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilterList;

public abstract class ObjMaskFilterDelegate extends ObjMaskFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskFilterList list = null;
	// END BEAN PROPERTIES
	
	protected ObjMaskFilterList delegate() {
		return list;
	}
	
	public ObjMaskFilterList getList() {
		return list;
	}

	public void setList(ObjMaskFilterList list) {
		this.list = list;
	}
}
