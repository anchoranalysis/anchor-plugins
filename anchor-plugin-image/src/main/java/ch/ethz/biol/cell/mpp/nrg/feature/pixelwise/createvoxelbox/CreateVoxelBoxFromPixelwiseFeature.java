/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxList;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class CreateVoxelBoxFromPixelwiseFeature {

    private VoxelBoxList listVoxelBox;
    private Optional<KeyValueParams> keyValueParams;
    private List<Histogram> listAdditionalHistograms;

    // Constructor
    public CreateVoxelBoxFromPixelwiseFeature(
            VoxelBoxList listVoxelBox,
            Optional<KeyValueParams> keyValueParams,
            List<Histogram> listAdditionalHistograms) {
        super();
        this.listVoxelBox = listVoxelBox;
        this.keyValueParams = keyValueParams;
        this.listAdditionalHistograms = listAdditionalHistograms;
    }

    public VoxelBox<ByteBuffer> createVoxelBoxFromPixelScore(PixelScore pixelScore, Logger logger)
            throws CreateException {

        // Sets up the Feature
        try {
            Extent e = listVoxelBox.getFirstExtent();

            // We make our index buffer
            VoxelBox<ByteBuffer> vbOut = VoxelBoxFactory.getByte().create(e);
            setPixels(vbOut, pixelScore, logger);
            return vbOut;

        } catch (InitException | FeatureCalcException e) {
            throw new CreateException(e);
        }
    }

    private void setPixels(VoxelBox<ByteBuffer> vbOut, PixelScore pixelScore, Logger logger)
            throws FeatureCalcException, InitException {

        pixelScore.init(createHistograms(), keyValueParams);

        Extent e = vbOut.extent();

        for (int z = 0; z < e.getZ(); z++) {

            List<VoxelBuffer<?>> bbList = listVoxelBox.bufferListForSlice(z);

            ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();

            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    int offset = e.offset(x, y);

                    BufferUtilities.putScoreForOffset(pixelScore, bbList, bbOut, offset);
                }
            }
        }
    }

    private List<Histogram> createHistograms() {
        List<Histogram> out = new ArrayList<>();

        for (VoxelBoxWrapper voxelBox : listVoxelBox) {
            out.add(HistogramFactory.create(voxelBox, Optional.empty()));
        }

        for (Histogram hist : listAdditionalHistograms) {
            out.add(hist);
        }

        return out;
    }
}
