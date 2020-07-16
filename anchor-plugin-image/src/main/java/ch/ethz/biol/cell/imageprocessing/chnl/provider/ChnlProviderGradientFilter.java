/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

//
/**
 * 3x3 Sobel Filter
 *
 * <p>Out-of-bounds strategy = mirror
 *
 * @author Owen Feehan
 */
public class ChnlProviderGradientFilter extends ChnlProviderGradientBase {

    @Override
    protected boolean[] createDimensionArr() {
        return new boolean[] {true, true, false};
    }
}
