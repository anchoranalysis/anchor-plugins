/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

public final class ChnlProviderAddConstant extends ChnlProviderOneValueArithmetic {

    @Override
    protected int binaryOp(int voxel, int constant) {
        return voxel + constant;
    }
}
