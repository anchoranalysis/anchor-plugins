/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever;

import org.anchoranalysis.core.error.AnchorCheckedException;

public class TraverseOutlineException extends AnchorCheckedException {

    /** */
    private static final long serialVersionUID = 4293810593116687378L;

    public TraverseOutlineException(String message) {
        super(message);
    }

    public TraverseOutlineException(String message, Throwable cause) {
        super(message, cause);
    }
}
