/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;

/** Performs an dilation morphological operation on a binary-image */
public class BinaryChnlProviderDilate extends BinaryChnlProviderMorphOp {

    // START
    @BeanField @Getter @Setter private boolean zOnly = false; // Only dilates in the z-direction

    @BeanField @Getter @Setter private boolean bigNeighborhood = false;
    // END

    // ! Checks that a mark's initial parameters are correct
    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (isSuppress3D() && zOnly) {
            throw new BeanMisconfiguredException(
                    "Either suppress3D and zOnly may be selected, but not both");
        }
    }

    // Assumes imgChnlOut has the same ImgChnlRegions
    @Override
    protected void applyMorphOp(Mask imgChnl, boolean do3D) throws CreateException {

        BinaryVoxelBox<ByteBuffer> out =
                MorphologicalDilation.dilate(
                        imgChnl.binaryVoxelBox(),
                        do3D,
                        getIterations(),
                        backgroundVb(),
                        getMinIntensityValue(),
                        zOnly,
                        false,
                        Optional.empty(),
                        bigNeighborhood);

        try {
            imgChnl.replaceBy(out);
        } catch (IncorrectImageSizeException e) {
            assert false;
        }
    }
}
