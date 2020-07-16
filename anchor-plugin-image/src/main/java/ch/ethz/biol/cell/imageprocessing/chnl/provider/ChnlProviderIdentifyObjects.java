/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// Assigns a unique id number to each objects
// Note behaviour is undefined when objects overlap with each other. An ID of either object
// arbitrarily will be assigned.
public class ChnlProviderIdentifyObjects extends ChnlProviderOneObjectsSource {

    @Override
    protected Channel createFromChnl(Channel chnl, ObjectCollection objectsSource)
            throws CreateException {

        VoxelBox<?> vb = chnl.getVoxelBox().any();

        if (objectsSource.size() > 255) {
            throw new CreateException(
                    String.format(
                            "A maximum of 255 objects is allowed. There are %d",
                            objectsSource.size()));
        }

        vb.setAllPixelsTo(0);

        int index = 1;
        for (ObjectMask objects : objectsSource) {
            vb.setPixelsCheckMask(objects, index++);
        }

        return chnl;
    }
}
