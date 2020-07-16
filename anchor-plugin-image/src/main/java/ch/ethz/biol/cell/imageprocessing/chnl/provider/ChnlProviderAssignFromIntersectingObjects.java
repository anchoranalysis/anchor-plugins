/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.apache.commons.math3.util.Pair;

// Matches source-objects to target objects, based upon intersection, and assigns the
//   value in the respective source object to the target object
public class ChnlProviderAssignFromIntersectingObjects extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsSource;

    @BeanField @Getter @Setter private ObjectCollectionProvider objectsTarget;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        VoxelBox<?> vb = chnl.getVoxelBox().any();

        ObjectCollection source = objectsSource.create();
        ObjectCollection target = objectsTarget.create();

        streamIntersectingObjects(source, target)
                .forEach(
                        pair ->
                                vb.setPixelsCheckMask(
                                        pair.getSecond(), getValForMask(chnl, pair.getFirst())));
        return chnl;
    }

    /**
     * Matches each object in {@code source} against {@code target} ensuring that it is a one-to-one
     * mapping
     *
     * @param source
     * @param target
     * @return a pair with source object (left) and the matched object (right)
     */
    private static Stream<Pair<ObjectMask, ObjectMask>> streamIntersectingObjects(
            ObjectCollection source, ObjectCollection target) {

        List<MatchedObject> matchList =
                MatcherIntersectionHelper.matchIntersectingObjects(source, target);

        return matchList.stream()
                .map(
                        owm ->
                                new Pair<>(
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

    private static int getValForMask(Channel chnl, ObjectMask object) {

        VoxelBox<?> vb = chnl.getVoxelBox().any();

        return vb.getVoxel(
                object.findArbitraryOnVoxel().orElseThrow(AnchorImpossibleSituationException::new));
    }
}
