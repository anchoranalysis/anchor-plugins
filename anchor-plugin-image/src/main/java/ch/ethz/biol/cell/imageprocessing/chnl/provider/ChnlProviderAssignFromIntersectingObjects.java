/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.extracter.VoxelsExtracter;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;

/**
 * Matches source-objects to target objects, based upon intersection, and assigns the value in the
 * respective source object to the target object
 *
 * @author Owen Feehan
 */
public class ChnlProviderAssignFromIntersectingObjects extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsSource;

    @BeanField @Getter @Setter private ObjectCollectionProvider objectsTarget;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {

        ObjectCollection source = objectsSource.create();
        ObjectCollection target = objectsTarget.create();

        VoxelsExtracter<?> extracter = channel.extract();

        streamIntersectingObjects(source, target)
                .forEach(
                        pair ->
                                channel.assignValue(getValueForObject(extracter, pair._1()))
                                        .toObject(pair._2()));
        return channel;
    }

    /**
     * Matches each object in {@code source} against {@code target} ensuring that it is a one-to-one
     * mapping
     *
     * @param source
     * @param target
     * @return a pair with source object (left) and the matched object (right)
     */
    private static Stream<Tuple2<ObjectMask, ObjectMask>> streamIntersectingObjects(
            ObjectCollection source, ObjectCollection target) {

        List<MatchedObject> matchList =
                MatcherIntersectionHelper.matchIntersectingObjects(source, target);

        return matchList.stream()
                .map(
                        owm ->
                                Tuple.of(
                                        owm.getSource(),
                                        selectBestMatch(owm.getSource(), owm.getMatches())));
    }

    private static ObjectMask selectBestMatch(ObjectMask source, ObjectCollection matches) {
        assert (matches.size() > 0);

        if (matches.size() == 1) {
            return matches.get(0);
        }

        int maxIntersection = -1;
        ObjectMask mostIntersecting = null;
        for (ObjectMask object : matches) {

            int intersectingVoxels = source.countIntersectingVoxels(object);
            if (intersectingVoxels > maxIntersection) {
                mostIntersecting = object;
                maxIntersection = intersectingVoxels;
            }
        }
        return mostIntersecting;
    }

    private static int getValueForObject(VoxelsExtracter<?> extracter, ObjectMask object) {
        return extracter.voxel(
                object.findArbitraryOnVoxel().orElseThrow(AnchorImpossibleSituationException::new));
    }
}
