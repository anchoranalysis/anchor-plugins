package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class CalculateLevelListBase extends CalculateLevel {

	// START BEAN PROPERTIES
	@BeanField
	private List<CalculateLevel> list = new ArrayList<>();
	// END BEAN PROPERTIES
	
	

	public List<CalculateLevel> getList() {
		return list;
	}

	public void setList(List<CalculateLevel> list) {
		this.list = list;
	}
	
	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		assert(false);
		return new HashCodeBuilder()
			.toHashCode();
	}
}
