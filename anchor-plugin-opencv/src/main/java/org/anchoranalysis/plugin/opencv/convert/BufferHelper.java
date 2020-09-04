package org.anchoranalysis.plugin.opencv.convert;

import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.channel.Channel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class BufferHelper {

    public static UnsignedByteBuffer bufferFromChannel(Channel channel) {
        return channel.voxels().asByte().sliceBuffer(0);
    }
}
