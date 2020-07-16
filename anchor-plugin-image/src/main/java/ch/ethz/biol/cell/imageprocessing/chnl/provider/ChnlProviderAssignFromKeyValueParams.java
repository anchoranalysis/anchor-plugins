/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderAssignFromKeyValueParams extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private String keyValueParamsID = "";

    @BeanField private String key;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        KeyValueParams params;
        try {
            params =
                    getInitializationParameters()
                            .getParams()
                            .getNamedKeyValueParamsCollection()
                            .getException(keyValueParamsID);
        } catch (NamedProviderGetException e) {
            throw new CreateException(
                    String.format("Cannot find KeyValueParams '%s'", keyValueParamsID), e);
        }

        if (!params.containsKey(key)) {
            throw new CreateException(String.format("Cannot find key '%s'", key));
        }

        byte valueByte = (byte) Double.parseDouble(params.getProperty(key));

        VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();

        int volumeXY = vb.extent().getVolumeXY();
        for (int z = 0; z < vb.extent().getZ(); z++) {
            ByteBuffer bb = vb.getPixelsForPlane(z).buffer();

            int offset = 0;
            while (offset < volumeXY) {
                bb.put(offset++, valueByte);
            }
        }

        return chnl;
    }

    public String getKeyValueParamsID() {
        return keyValueParamsID;
    }

    public void setKeyValueParamsID(String keyValueParamsID) {
        this.keyValueParamsID = keyValueParamsID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
