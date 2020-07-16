/* (C)2020 */
package ch.ethz.biol.cell.mpp.anneal;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;

public class AnnealSchemeNone extends AnnealScheme {

    // Returns identity
    @Override
    public final double calcTemp(int iter) {
        return 1.0;
    }

    @Override
    public final double crntTemp() {
        return 1.0;
    }
}
