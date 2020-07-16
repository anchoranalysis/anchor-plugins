/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;

/** Performs an erosion morphological operation on a binary-image */
public class BinaryChnlProviderErode extends BinaryChnlProviderMorphOp {

    // START
    @BeanField private boolean outsideAtThreshold = true;
    // END

    // Assumes imgChnlOut has the same ImgChnlRegions
    @Override
    protected void applyMorphOp(Mask imgChnl, boolean do3D) throws CreateException {

        BinaryVoxelBox<ByteBuffer> out =
                MorphologicalErosion.erode(
                        imgChnl.binaryVoxelBox(),
                        do3D,
                        getIterations(),
                        backgroundVb(),
                        getMinIntensityValue(),
                        outsideAtThreshold,
                        Optional.empty());

        try {
            imgChnl.replaceBy(out);
        } catch (IncorrectImageSizeException e) {
            assert false;
        }
    }

    public boolean isOutsideAtThreshold() {
        return outsideAtThreshold;
    }

    public void setOutsideAtThreshold(boolean outsideAtThreshold) {
        this.outsideAtThreshold = outsideAtThreshold;
    }
}
