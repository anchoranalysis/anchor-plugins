package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.geometry.Point3i;

class Minima {

	private List<Point3i> delegate;
	
	public Minima( List<Point3i> pntList ) {
		delegate = pntList;
	}
	
	public Minima( Point3i pnt ) {
		delegate = new ArrayList<>();
		delegate.add(pnt);
	}

	public List<Point3i> getListPnts() {
		return delegate;
	}


}