package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.stack.Stack;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BackgroundHelper {

    public static Optional<Stack> determineBackgroundAndScale(
            Optional<Stack> backgroundSource, int backgroundChannelIndex, FlattenAndScaler scaler) {
        return backgroundSource.flatMap(
                source ->
                        scaler.scaleStack(
                                BackgroundHelper.determineBackground(
                                        source, backgroundChannelIndex)));
    }

    /** Derives a background-stack from a stack that is a source of possible backgrounds */
    private static Optional<Stack> determineBackground(
            Stack backgroundSource, int backgroundChannelIndex) {

        if (backgroundChannelIndex > -1) {
            return Optional.of(extractChannelAsStack(backgroundSource, backgroundChannelIndex));
        }

        int numberChannels = backgroundSource.getNumberChannels();
        if (numberChannels == 0) {
            return Optional.empty();
        } else if (numberChannels == 3 || numberChannels == 1) {
            return Optional.of(backgroundSource);
        } else {
            return Optional.of(extractChannelAsStack(backgroundSource, 0));
        }
    }

    private static Stack extractChannelAsStack(Stack stack, int index) {
        return new Stack(stack.getChannel(index));
    }
}
