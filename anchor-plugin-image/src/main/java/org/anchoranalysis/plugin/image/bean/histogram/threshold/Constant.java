/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.histogram.threshold;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A constant value for thresholding
 *
 * @author Owen Feehan
 */
public class Constant extends CalculateLevel {

    // START BEAN PROPERTIES
    @BeanField @NonNegative private int level = 0;
    // END BEAN PROPERTIES

    public Constant() {}

    public Constant(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int calculateLevel(Histogram h) {
        return level;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Constant) {
            final Constant other = (Constant) obj;
            return new EqualsBuilder().append(level, other.level).isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(level).toHashCode();
    }
}
