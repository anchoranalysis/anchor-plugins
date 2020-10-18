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

package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedIntBuffer;
import org.anchoranalysis.spatial.extent.Extent;
import org.anchoranalysis.spatial.point.Point3i;

@AllArgsConstructor
@Accessors(fluent = true)
public final class EncodedVoxels {

    public static final WatershedEncoding ENCODING = new WatershedEncoding();

    @Getter private final Voxels<UnsignedIntBuffer> voxels;

    public void setPoint(Point3i point, int code) {
        int offset = voxels.extent().offset(point.x(), point.y());
        voxels.sliceBuffer(point.z()).putRaw(offset, code);
    }

    public void setPointConnectedComponentID(Point3i point, int connectedComponentID) {
        setPoint(point, ENCODING.encodeConnectedComponentID(connectedComponentID));
    }

    public void setPointDirection(Point3i point, int xChange, int yChange, int zChange) {
        setPoint(point, ENCODING.encodeDirection(xChange, yChange, zChange));
    }

    /**
     * Sets all points in a list, to point at the first point (the root point) in the list
     *
     * @param points the list
     */
    public void pointListAtFirstPoint(List<Point3i> points) {

        Point3i rootPoint = points.get(0);
        int rootPointOffsetEncoded =
                ENCODING.encodeConnectedComponentID(voxels.extent().offset(rootPoint));
        setPoint(rootPoint, rootPointOffsetEncoded);

        for (int i = 1; i < points.size(); i++) {
            setPoint(points.get(i), rootPointOffsetEncoded);
        }
    }

    public EncodedIntBuffer getPixelsForPlane(int z) {
        return new EncodedIntBuffer(voxels.sliceBuffer(z), ENCODING);
    }

    public boolean hasTemporary() {
        int volumeXY = extent().volumeXY();

        for (int z = 0; z < extent().z(); z++) {
            EncodedIntBuffer buffer = getPixelsForPlane(z);

            for (int i = 0; i < volumeXY; i++) {
                if (buffer.isTemporary(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Point3i> getTemporary() {

        ArrayList<Point3i> listOut = new ArrayList<>();

        for (int z = 0; z < extent().z(); z++) {
            EncodedIntBuffer buffer = getPixelsForPlane(z);

            int offset = 0;
            for (int y = 0; y < extent().y(); y++) {
                for (int x = 0; x < extent().x(); x++) {

                    if (buffer.isTemporary(offset++)) {
                        listOut.add(new Point3i(x, y, z));
                    }
                }
            }
        }
        return listOut;
    }

    // This is a debug method
    public Set<Integer> setOfConnectedComponentIDs() {

        Set<Integer> setOut = new HashSet<>();

        for (int z = 0; z < extent().z(); z++) {
            EncodedIntBuffer buffer = getPixelsForPlane(z);

            int offset = 0;
            for (int y = 0; y < extent().y(); y++) {
                for (int x = 0; x < extent().x(); x++) {

                    if (buffer.isConnectedComponentID(offset)) {
                        setOut.add(buffer.getCode(offset));
                    }
                    offset++;
                }
            }
        }
        return setOut;
    }

    // Returns the CODED final index (global)
    public int calculateConnectedComponentID(Point3i point, int firstChainCode) {

        Extent extent = voxels.extent();

        int crntChainCode = firstChainCode;

        do {
            // Get local index from global index
            point = addDirectionFromChainCode(point, crntChainCode);

            assert (extent.contains(point)); // NOSONAR

            // Replace with intelligence slices buffer?
            int nextVal = voxels.sliceBuffer(point.z()).getRaw(extent.offsetSlice(point));

            assert (nextVal != WatershedEncoding.CODE_UNVISITED);
            assert (nextVal != WatershedEncoding.CODE_TEMPORARY);

            if (ENCODING.isConnectedComponentIDCode(nextVal)) {
                return nextVal;
            }

            if (nextVal == WatershedEncoding.CODE_MINIMA) {
                return ENCODING.encodeConnectedComponentID(extent.offset(point));
            }

            crntChainCode = nextVal;

        } while (true);
    }

    /**
     * Adds a direction to a point from a chain-code, without altering the incoming point
     *
     * @param point an input-point that is unchanged (immutable)
     * @param chainCode a chain-code from which to decode a direction, which is added to point
     * @return a new point which is the sum of point and the decoded-direction
     */
    private Point3i addDirectionFromChainCode(Point3i point, int chainCode) {
        Point3i out = ENCODING.chainCodes(chainCode);
        out.add(point);
        return out;
    }

    public boolean isPlateau(int code) {
        return code == WatershedEncoding.CODE_PLATEAU;
    }

    public boolean isMinima(int code) {
        return code == WatershedEncoding.CODE_MINIMA;
    }

    public boolean isTemporary(int code) {
        return code == WatershedEncoding.CODE_TEMPORARY;
    }

    public boolean isUnvisited(int code) {
        return code == WatershedEncoding.CODE_UNVISITED;
    }

    public boolean isDirectionChainCode(int code) {
        return ENCODING.isDirectionChainCode(code);
    }

    public boolean isConnectedComponentIDCode(int code) {
        return ENCODING.isConnectedComponentIDCode(code);
    }

    public Extent extent() {
        return voxels.extent();
    }
}
