/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox;

import java.nio.ByteBuffer;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BufferUtilities {

    public static void putScoreForOffset(
            PixelScore pixelScore, List<VoxelBuffer<?>> bbList, ByteBuffer bbOut, int offset)
            throws FeatureCalcException {
        double score = pixelScore.calc(createParams(bbList, offset));

        int scoreInt = (int) Math.round(score * 255);
        bbOut.put(offset, (byte) scoreInt);
    }

    private static int[] createParams(List<VoxelBuffer<?>> bbList, int offset) {

        int[] vals = new int[bbList.size()];

        for (int c = 0; c < bbList.size(); c++) {
            vals[c] = bbList.get(c).getInt(offset);
        }

        return vals;
    }
}
