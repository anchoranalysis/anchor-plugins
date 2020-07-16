/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline;

import java.util.ArrayList;
import java.util.Set;
import org.anchoranalysis.image.contour.Contour;

public class ContourList extends ArrayList<Contour> {

    /** */
    private static final long serialVersionUID = 7003021223351541824L;

    // Create one large contour with all the points
    public Contour createCombinedContour() {
        Contour out = new Contour();
        for (Contour contour : this) {
            contour.getPoints().stream().forEach(out.getPoints()::add);
        }
        return out;
    }

    public int numberFoundFrom(Set<Contour> set) {
        int cnt = 0;
        for (Contour contour : this) {
            if (set.contains(contour)) {
                cnt++;
            }
        }
        return cnt;
    }

    public Contour getFirst() {
        return get(0);
    }

    public Contour getLast() {
        return get(size() - 1);
    }

    public boolean firstConnectedTo(ContourList other) {
        return getFirst().connectedTo(other.getLast());
    }

    public boolean lastConnectedTo(ContourList other) {
        return getLast().connectedTo(other.getFirst());
    }

    @Override
    public String toString() {
        String newLine = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder();
        sb.append("--" + newLine);
        for (int i = 0; i < size(); i++) {
            Contour contour = get(i);
            sb.append(contour.summaryStr() + newLine);
        }

        sb.append("--" + newLine);
        return sb.toString();
    }
}
