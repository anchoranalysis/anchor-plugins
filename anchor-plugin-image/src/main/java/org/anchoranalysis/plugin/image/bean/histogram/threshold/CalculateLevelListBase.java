package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CalculateLevelListBase other = (CalculateLevelListBase) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		return true;
	}
}
