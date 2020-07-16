/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider.level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.core.geometry.Point3d;

public class LevelResultCollection implements Iterable<LevelResult> {

    private List<LevelResult> list = new ArrayList<>();

    public boolean add(LevelResult arg0) {
        return list.add(arg0);
    }

    public LevelResult findClosestResult(Point3d point) {

        double minDistance = Double.MAX_VALUE;
        LevelResult min = null;

        for (LevelResult levelResult : list) {

            double distance = levelResult.distanceSquaredTo(point);
            if (distance < minDistance) {
                minDistance = distance;
                min = levelResult;
            }
        }
        return min;
    }

    @Override
    public Iterator<LevelResult> iterator() {
        return list.iterator();
    }
}
