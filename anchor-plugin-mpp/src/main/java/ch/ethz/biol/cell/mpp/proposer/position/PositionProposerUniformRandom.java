/* (C)2020 */
package ch.ethz.biol.cell.mpp.proposer.position;

import java.util.Optional;
import java.util.function.ToIntFunction;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.PositionProposerBean;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.extent.ImageDimensions;

public class PositionProposerUniformRandom extends PositionProposerBean {

    // START BEAN
    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN

    @Override
    public Optional<Point3d> propose(ProposerContext context) {

        double x = sampleAlongAxis(context, ImageDimensions::getX);
        double y = sampleAlongAxis(context, ImageDimensions::getY);
        double z = suppressZ ? 0 : sampleAlongAxis(context, ImageDimensions::getZ);
        return Optional.of(new Point3d(x, y, z));
    }

    private static double sampleAlongAxis(
            ProposerContext context, ToIntFunction<ImageDimensions> extentForAxis) {
        return context.getRandomNumberGenerator()
                .sampleDoubleFromRange(extentForAxis.applyAsInt(context.getDimensions()));
    }
}
