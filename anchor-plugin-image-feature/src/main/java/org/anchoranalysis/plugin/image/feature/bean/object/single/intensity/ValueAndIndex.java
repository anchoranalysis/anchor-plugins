/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

class ValueAndIndex {
    private double value;
    private int index;

    public ValueAndIndex(double value, int index) {
        super();
        this.value = value;
        this.index = index;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
