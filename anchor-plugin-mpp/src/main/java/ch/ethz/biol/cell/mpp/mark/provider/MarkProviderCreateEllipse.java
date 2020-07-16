/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.provider;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.image.orientation.Orientation2D;

public class MarkProviderCreateEllipse extends MarkProvider {

    // START BEAN PROPERTIES
    @BeanField private double shellRad = 0.2;

    @BeanField private int id = 0;
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> create() throws CreateException {
        MarkEllipse mark = new MarkEllipse();
        mark.setMarks(new Point2d(5, 5), new Orientation2D(0));
        mark.setId(id);
        mark.updateShellRad(shellRad);
        return Optional.of(mark);
    }

    public double getShellRad() {
        return shellRad;
    }

    public void setShellRad(double shellRad) {
        this.shellRad = shellRad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
