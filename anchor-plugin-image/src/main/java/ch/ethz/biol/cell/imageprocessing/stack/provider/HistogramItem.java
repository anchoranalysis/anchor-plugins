/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.core.index.IIndexGetter;

class HistogramItem implements IIndexGetter {

    private int intensity;
    private int count;

    public HistogramItem(int intensity, int count) {
        super();
        this.intensity = intensity;
        this.count = count;
    }

    @Override
    public int getIndex() {
        return intensity;
    }

    public int getCount() {
        return count;
    }
}
