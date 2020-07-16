/* (C)2020 */
package ch.ethz.biol.cell.mpp.bound;

import java.util.HashMap;
import lombok.RequiredArgsConstructor;

/**
 * @author Owen Feehan
 * @param <T> store-type
 */
@RequiredArgsConstructor
class AngleStore<T> {

    private final int precisionMultiplier;

    private HashMap<Integer, T> map = new HashMap<>();

    public int cnvrtToIndex(double angle) {
        return (int) (angle * precisionMultiplier);
    }

    public double cnvrtToAngle(int index) {
        double dbl = index;
        return dbl / precisionMultiplier;
    }

    public T get(int index) {
        return map.get(index);
    }

    public void put(int index, T item) {
        map.put(index, item);
    }
}
