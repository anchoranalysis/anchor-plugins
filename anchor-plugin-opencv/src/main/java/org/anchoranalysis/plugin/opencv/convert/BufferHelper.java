package org.anchoranalysis.plugin.opencv.convert;

import java.nio.ByteBuffer;
import org.anchoranalysis.image.channel.Channel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class BufferHelper {

    public static ByteBuffer bufferFromChannel(Channel channel) {
        return channel.voxels().asByte().sliceBuffer(0);
    }
}
