package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;

public abstract class CalculateLevelRelativeToThreshold extends CalculateLevelOne {

	// START BEAN PROPERTIES
	@BeanField
	private CalculateLevel calculateLevelElse;
	
	@BeanField
	private int threshold;
	// END BEAN PROPERTIES
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {

		int level = calculateLevelIncoming(h);
		if (useElseInstead(level,threshold)) {
			return calculateLevelElse.calculateLevel(h);
		} else {
			return level;
		}
	}
	
	/** Uses the {@link calculateLevelElse} instead of {@link calculateLevel} */
	protected abstract boolean useElseInstead(int level, int threshold);
	
	public CalculateLevel getCalculateLevelElse() {
		return calculateLevelElse;
	}
	public void setCalculateLevelElse(CalculateLevel calculateLevelElse) {
		this.calculateLevelElse = calculateLevelElse;
	}
	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((calculateLevelElse == null) ? 0 : calculateLevelElse.hashCode());
		result = prime * result + threshold;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CalculateLevelRelativeToThreshold other = (CalculateLevelRelativeToThreshold) obj;
		if (calculateLevelElse == null) {
			if (other.calculateLevelElse != null)
				return false;
		} else if (!calculateLevelElse.equals(other.calculateLevelElse))
			return false;
		if (threshold != other.threshold)
			return false;
		return true;
	}
}
