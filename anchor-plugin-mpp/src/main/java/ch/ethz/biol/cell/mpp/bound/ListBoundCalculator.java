/* (C)2020 */
package ch.ethz.biol.cell.mpp.bound;

import java.util.ArrayList;
import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;

public class ListBoundCalculator extends ArrayList<BoundCalculator> {

    /** */
    private static final long serialVersionUID = -6980387308682133678L;

    // probMap is the thresholded outlineMap
    public void init(MPPInitParams so, Logger logger) throws InitException {

        for (BoundCalculator bc : this) {
            bc.initRecursive(so, logger);
        }
    }
}
