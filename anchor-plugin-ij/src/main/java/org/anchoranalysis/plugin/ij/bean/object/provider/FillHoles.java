/* (C)2020 */
package org.anchoranalysis.plugin.ij.bean.object.provider;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderIJBinary;
import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Fills holes in an object. Existing object-masks are overwritten (i.e. their memory buffers are
 * replaced with filled-in pixels).
 *
 * <p>An optional mask which restricts where a fill operation can happen TODO make this an immutable
 * provider that always returns a new object-collection.
 *
 * @author Owen Feehan
 */
public class FillHoles extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** */
    @BeanField @OptionalBean @Getter @Setter private BinaryChnlProvider mask;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        Optional<Mask> maskChnl = OptionalFactory.create(mask);

        for (ObjectMask objectMask : objects) {

            BinaryVoxelBox<ByteBuffer> bvb = objectMask.binaryVoxelBox();
            BinaryVoxelBox<ByteBuffer> bvbDup = bvb.duplicate();

            try {
                BinaryChnlProviderIJBinary.fill(bvbDup);
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }

            if (maskChnl.isPresent()) {
                // Let's make an object for our mask
                ObjectMask objectRegion = maskChnl.get().region(objectMask.getBoundingBox(), true);

                BoundingBox bboxAll = new BoundingBox(bvb.extent());

                // We do an and operation with the mask
                bvbDup.copyPixelsToCheckMask(
                        bboxAll,
                        bvb.getVoxelBox(),
                        bboxAll,
                        objectRegion.getVoxelBox(),
                        objectRegion.getBinaryValuesByte());
            }
        }
        return objects;
    }
}
