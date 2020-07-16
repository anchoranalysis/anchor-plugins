/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.chnl.level;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResult;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollection;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.level.LevelResultCollectionFactory;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// Calculates a threshold-level for each object independently
public class ChnlProviderObjectsLevelIndependently extends ChnlProviderLevel {

    // START BEAN
    @BeanField @Getter @Setter private int numDilations = 0;
    // END BEAN

    @Override
    protected Channel createFor(Channel chnlIntensity, ObjectCollection objects, Channel chnlOutput)
            throws CreateException {

        try {
            LevelResultCollection lrc =
                    LevelResultCollectionFactory.createCollection(
                            chnlIntensity,
                            objects,
                            getCalculateLevel(),
                            numDilations,
                            getLogger().messageLogger());

            VoxelBox<?> vbOutput = chnlOutput.getVoxelBox().any();
            for (LevelResult lr : lrc) {
                vbOutput.setPixelsCheckMask(lr.getObject(), lr.getLevel());
            }

            return chnlOutput;

        } catch (CreateException e) {
            throw new CreateException(e);
        }
    }
}
