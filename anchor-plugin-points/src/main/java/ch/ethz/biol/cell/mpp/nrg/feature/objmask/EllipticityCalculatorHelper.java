/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EllipticityCalculatorHelper {

    public static double calc(ObjectMask object, Mark mark, ImageDimensions dimensions) {
        return calc(object, maskCompare(mark, dimensions));
    }

    private static double calc(ObjectMask object, ObjectMask objectCompare) {
        return calcWithMerged(object, objectCompare, ObjectMaskMerger.merge(object, objectCompare));
    }

    private static double calcWithMerged(
            ObjectMask object, ObjectMask objectCompare, ObjectMask merged) {
        int numPixelsCompare = objectCompare.numberVoxelsOn();
        int numUnion = merged.numberVoxelsOn();

        // Interseting pixels
        int numIntersection = object.countIntersectingVoxels(objectCompare);

        return intDiv(numPixelsCompare, numUnion - numIntersection + numPixelsCompare);
    }

    private static ObjectMask maskCompare(Mark mark, ImageDimensions dim) {
        RegionMembershipWithFlags rm = RegionMapSingleton.instance().membershipWithFlagsForIndex(0);
        assert (rm.getRegionID() == 0);
        return mark.calcMask(dim, rm, BinaryValuesByte.getDefault()).getMask();
    }

    private static double intDiv(int num, int dem) {
        return ((double) num) / dem;
    }
}
