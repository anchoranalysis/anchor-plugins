/* (C)2020 */
package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnSequence extends BinarySegmentation {

    // START BEAN PROPERTIES
    @OptionalBean private List<BinarySegmentation> listSgmn = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (listSgmn.isEmpty()) {
            throw new BeanMisconfiguredException("At least one item is required in listSgmn");
        }
    }

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmn(
            VoxelBoxWrapper voxelBox,
            BinarySegmentationParameters params,
            Optional<ObjectMask> mask)
            throws SegmentationFailedException {

        BinaryVoxelBox<ByteBuffer> out = null;

        // A bounding-box capturing what part of the scene is being segmented
        BoundingBox bbox =
                mask.map(ObjectMask::getBoundingBox)
                        .orElseGet(() -> new BoundingBox(voxelBox.any().extent()));

        // A mask that evolves as we move through each segmentation to be increasingly smaller.
        Optional<ObjectMask> evolvingMask = mask;
        for (BinarySegmentation sgmn : listSgmn) {

            BinaryVoxelBox<ByteBuffer> outNew = sgmn.sgmn(voxelBox, params, evolvingMask);

            out = outNew;
            evolvingMask = Optional.of(new ObjectMask(bbox, outNew));
        }

        assert (out != null);

        return out;
    }

    public List<BinarySegmentation> getListSgmn() {
        return listSgmn;
    }

    public void setListSgmn(List<BinarySegmentation> listSgmn) {
        this.listSgmn = listSgmn;
    }
}
