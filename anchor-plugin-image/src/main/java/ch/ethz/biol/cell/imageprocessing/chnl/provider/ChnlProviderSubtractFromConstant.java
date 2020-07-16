/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

public class ChnlProviderSubtractFromConstant extends ChnlProviderOneValueArithmetic {

    @Override
    protected int binaryOp(int voxel, int constant) {
        return constant - voxel;
    }
}
