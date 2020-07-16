/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import com.google.common.collect.BoundType;
import com.google.common.collect.TreeMultiset;
import java.nio.ByteBuffer;
import java.util.Iterator;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// 3x3 Sobel Filter
public class ChnlProviderMedianFilter2D extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private int kernelHalfWidth;
    // END BEAN PROPERTIES

    // Uses a square kernel of size (2*kernelHalfWidth+1)x(2*kernelHalfWidth+1)
    private static class RollingMultiSet {

        private TreeMultiset<Integer> set = TreeMultiset.create();
        private int kernelHalfWidth;

        public RollingMultiSet(int kernelHalfWidth) {
            super();
            this.kernelHalfWidth = kernelHalfWidth;
        }

        public void clear() {
            set.clear();
        }

        public void ppltAt(int xCenter, int yMin, int yMax, ByteBuffer bb, Extent e) {

            int xMin = xCenter - kernelHalfWidth;
            int xMax = xCenter + kernelHalfWidth;

            xMin = Math.max(xMin, 0);
            xMax = Math.min(xMax, e.getX() - 1);

            for (int y = yMin; y <= yMax; y++) {
                for (int x = xMin; x <= xMax; x++) {

                    int val = ByteConverter.unsignedByteToInt(bb.get(e.offset(x, y)));
                    set.add(val);
                }
            }
        }

        public void removeColumn(int x, int yMin, int yMax, ByteBuffer bb, Extent e) {

            if (x < 0) {
                return;
            }
            if (x >= e.getX()) {
                return;
            }

            for (int y = yMin; y <= yMax; y++) {
                int val = ByteConverter.unsignedByteToInt(bb.get(e.offset(x, y)));
                assert (set.contains(val));
                set.remove(val);
            }
        }

        public void addColumn(int x, int yMin, int yMax, ByteBuffer bb, Extent e) {

            if (x < 0) {
                return;
            }
            if (x >= e.getX()) {
                return;
            }

            for (int y = yMin; y <= yMax; y++) {
                int val = ByteConverter.unsignedByteToInt(bb.get(e.offset(x, y)));
                set.add(val);
            }
        }

        public int calcMedian() {
            int size = size();
            int medianIndex = size / 2;

            Iterator<Integer> itr = set.headMultiset(255, BoundType.CLOSED).iterator();

            for (int i = 1; i < medianIndex; i++) {
                itr.next();
            }

            return itr.next();
        }

        public int size() {
            return set.headMultiset(255, BoundType.CLOSED).size();
        }
    }

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();

        RollingMultiSet set = new RollingMultiSet(kernelHalfWidth);

        Channel dup = chnl.duplicate();
        VoxelBox<ByteBuffer> vbDup = dup.getVoxelBox().asByte();
        Extent e = dup.getDimensions().getExtent();

        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
            ByteBuffer bbOut = vbDup.getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {

                int yMin = y - kernelHalfWidth;
                int yMax = y + kernelHalfWidth;

                yMin = Math.max(yMin, 0);
                yMax = Math.min(yMax, e.getY() - 1);

                for (int x = 0; x < e.getX(); x++) {

                    if (x == 0) {
                        set.clear();
                        set.ppltAt(x, yMin, yMax, bb, e);
                    } else {
                        set.removeColumn(x - kernelHalfWidth - 1, yMin, yMax, bb, e);
                        set.addColumn(x + kernelHalfWidth, yMin, yMax, bb, e);
                    }

                    int median = set.calcMedian();

                    bbOut.put(offset, (byte) median);
                    offset++;
                }
            }
        }
        return dup;
    }

    public int getKernelHalfWidth() {
        return kernelHalfWidth;
    }

    public void setKernelHalfWidth(int kernelHalfWidth) {
        this.kernelHalfWidth = kernelHalfWidth;
    }
}
