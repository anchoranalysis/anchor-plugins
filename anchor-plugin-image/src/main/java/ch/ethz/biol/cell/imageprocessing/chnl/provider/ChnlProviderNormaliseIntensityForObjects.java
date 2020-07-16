/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

// Rewrites the intensity for each object-mask (assume no overlap) so that its mean is 128
public class ChnlProviderNormaliseIntensityForObjects extends ChnlProviderOneObjectsSource {

    @Override
    protected Channel createFromChnl(Channel chnl, ObjectCollection objectsSource)
            throws CreateException {

        VoxelBox<?> vb = chnl.getVoxelBox().any();

        for (ObjectMask object : objectsSource) {

            try {
                double meanIntensity =
                        IntensityMeanCalculator.calcMeanIntensityObject(chnl, object);

                if (meanIntensity == 0.0) {
                    // Special case. The mean can only be 0.0, if all pixels are 0
                    vb.setPixelsCheckMask(object, 128);
                    continue;
                }

                double scaleFactor = 128 / meanIntensity;
                vb.scalePixelsCheckMask(object, scaleFactor);
            } catch (FeatureCalcException e) {
                throw new CreateException(e);
            }
        }

        return chnl;
    }
}
