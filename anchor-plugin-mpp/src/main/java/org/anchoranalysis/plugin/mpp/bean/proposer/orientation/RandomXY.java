/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.OrientationAxisAngle;

public class RandomXY extends OrientationProposer {

    @Override
    public Optional<Orientation> propose(
            Mark mark, ImageDimensions dimensions, RandomNumberGenerator randomNumberGenerator) {
        return Optional.of(
                new OrientationAxisAngle(
                        new Vector3d(0, 0, 1),
                        randomNumberGenerator.sampleDoubleFromRange(Math.PI * 2)));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }
}
