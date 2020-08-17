/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity.gradient;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

/**
 * When the image-gradient is supplied as multiple channels in an NRG stack, this converts it into a
 * list of points
 *
 * <p>A constant is subtracted from the (all positive) image-channels, to make positive or negative
 * values
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateGradientFromMultipleChnls
        extends FeatureCalculation<List<Point3d>, FeatureInputSingleObject> {

    private int nrgIndexX;

    private int nrgIndexY;

    // If -1, then no z-gradient is considered, and all z-values are 0
    private int nrgIndexZ;

    private int subtractConstant = 0;

    @Override
    protected List<Point3d> execute(FeatureInputSingleObject input)
            throws FeatureCalculationException {

        if (nrgIndexX == -1 || nrgIndexY == -1) {
            throw new FeatureCalculationException(
                    new InitException("nrgIndexX and nrgIndexY must both be nonZero"));
        }

        // create a list of points
        List<Point3d> out = new ArrayList<>();

        NRGStack nrgStack = input.getNrgStackRequired().getNrgStack();

        putGradientValue(input.getObject(), out, 0, nrgStack.getChannel(nrgIndexX));
        putGradientValue(input.getObject(), out, 1, nrgStack.getChannel(nrgIndexY));

        if (nrgIndexZ != -1) {
            putGradientValue(input.getObject(), out, 2, nrgStack.getChannel(nrgIndexZ));
        }

        return out;
    }

    // Always iterates over the list in the same-order
    private void putGradientValue(
            ObjectMask object, List<Point3d> points, int axisIndex, Channel chnl) {

        BinaryVoxels<ByteBuffer> bvb = object.binaryVoxels();
        Voxels<?> voxels = chnl.voxels().any();
        BoundingBox box = object.boundingBox();

        Extent e = voxels.extent();
        Extent eMask = box.extent();

        BinaryValuesByte bvbMask = bvb.binaryValues().createByte();

        // Tracks where are writing to on the output list.
        int pointIndex = 0;

        for (int z = 0; z < eMask.z(); z++) {

            VoxelBuffer<?> bb = voxels.slice(z + box.cornerMin().z());
            VoxelBuffer<ByteBuffer> bbMask = bvb.voxels().slice(z);

            for (int y = 0; y < eMask.y(); y++) {
                for (int x = 0; x < eMask.x(); x++) {

                    int offsetMask = eMask.offset(x, y);

                    if (bbMask.buffer().get(offsetMask) == bvbMask.getOnByte()) {

                        int offset = e.offset(x + box.cornerMin().x(), y + box.cornerMin().y());

                        int gradVal = bb.getInt(offset) - subtractConstant;

                        modifyOrAddPoint(points, pointIndex, gradVal, axisIndex);
                        pointIndex++;
                    }
                }
            }
        }
        assert (points.size() == pointIndex);
    }

    private static void modifyOrAddPoint(
            List<Point3d> points, int pointIndex, int gradVal, int axisIndex) {
        Point3d out = null;
        if (pointIndex == points.size()) {
            out = new Point3d(0, 0, 0);
            points.add(out);
        } else {
            out = points.get(pointIndex);
        }
        assert (out != null);

        out.setValueByDimension(axisIndex, gradVal);
    }
}
