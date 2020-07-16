/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxel;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedIntBuffer;

final class PointPixelsOrMarkAsMinima implements ProcessVoxel {

    private final SlidingBufferPlus bufferPlus;

    private EncodedIntBuffer bbS;

    public PointPixelsOrMarkAsMinima(SlidingBufferPlus bufferPlus) {
        super();
        this.bufferPlus = bufferPlus;
    }

    @Override
    public void process(Point3i point) {

        int indxBuffer = bufferPlus.offsetSlice(point);

        // Exit early if this voxel has already been visited
        if (!bbS.isUnvisited(indxBuffer)) {
            return;
        }

        // We get the value of g
        int gVal = bufferPlus.getG(indxBuffer);

        // Calculate steepest descent. -1 indicates that there is no steepest descent
        int chainCode = bufferPlus.calcSteepestDescent(point, gVal, indxBuffer);

        if (bufferPlus.isMinima(chainCode)) {
            // Treat as local minima
            bbS.putCode(indxBuffer, chainCode);
            bufferPlus.maybeAddMinima(point);

        } else if (bufferPlus.isPlateau(chainCode)) {
            bufferPlus.makePlateauAt(point);
        } else {
            // Record steepest
            bbS.putCode(indxBuffer, chainCode);
        }
    }

    @Override
    public void notifyChangeZ(int z) {
        bbS = bufferPlus.getSPlane(z);
    }
}
