/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

/** Base class for performing morphological operations */
public abstract class BinaryChnlProviderMorphOp extends BinaryChnlProviderOne {

    // START PROPERTIES
    @BeanField @OptionalBean private ChnlProvider backgroundChnlProvider;

    @BeanField @Positive private int iterations = 1;

    @BeanField private int minIntensityValue = 0;

    @BeanField private boolean suppress3D = false;
    // END PROPERTIES

    protected abstract void applyMorphOp(Mask imgChnl, boolean do3D) throws CreateException;

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {

        // Gets outline
        applyMorphOp(chnl, (chnl.getDimensions().getZ() > 1) && !suppress3D);

        return chnl;
    }

    protected Optional<VoxelBox<ByteBuffer>> backgroundVb() throws CreateException {

        if (minIntensityValue > 0) {
            return Optional.of(backgroundChnlProvider.create().getVoxelBox().asByte());
        } else {
            return Optional.empty();
        }
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getMinIntensityValue() {
        return minIntensityValue;
    }

    public void setMinIntensityValue(int minIntensityValue) {
        this.minIntensityValue = minIntensityValue;
    }

    public ChnlProvider getBackgroundChnlProvider() {
        return backgroundChnlProvider;
    }

    public void setBackgroundChnlProvider(ChnlProvider backgroundChnlProvider) {
        this.backgroundChnlProvider = backgroundChnlProvider;
    }

    public boolean isSuppress3D() {
        return suppress3D;
    }

    public void setSuppress3D(boolean suppress3d) {
        suppress3D = suppress3d;
    }
}
