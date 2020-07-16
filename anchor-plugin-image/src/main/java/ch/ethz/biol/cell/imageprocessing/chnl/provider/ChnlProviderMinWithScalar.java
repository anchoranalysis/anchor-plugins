/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

public class ChnlProviderMinWithScalar extends ChnlProviderConditionallyWriteScalar {

    @Override
    public boolean shouldOverwriteVoxelWithConstant(int voxel, int constant) {
        return voxel > constant;
    }
}
