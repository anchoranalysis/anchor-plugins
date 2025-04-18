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
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * Represents voxels encoded with watershed-related information.
 * 
 * <p>See {@link EncodedVoxels#ENCODING} for the type of information stored
 * in each voxel.
 */
@AllArgsConstructor
@Accessors(fluent = true)
public final class EncodedVoxels {

    /** The encoding used for the direction-vectors. */
    public static final WatershedEncoding ENCODING = new WatershedEncoding();

    /** The voxels containing encoded information. */
    @Getter private final Voxels<UnsignedIntBuffer> voxels;

    /**
     * Sets the encoded value for a specific point.
     *
     * @param point the 3D point to set
     * @param code the encoded value to set
     */
    public void setPoint(Point3i point, int code) {
        int offset = voxels.extent().offset(point.x(), point.y());
        voxels.sliceBuffer(point.z()).putRaw(offset, code);
    }

    /**
     * Sets the connected component ID for a specific point.
     *
     * @param point the 3D point to set
     * @param connectedComponentID the ID of the connected component
     */
    public void setPointConnectedComponentID(Point3i point, int connectedComponentID) {
        setPoint(point, ENCODING.encodeConnectedComponentID(connectedComponentID));
    }

    /**
     * Sets the direction for a specific point.
     *
     * @param point the 3D point to set
     * @param xChange change in x direction
     * @param yChange change in y direction
     * @param zChange change in z direction
     */
    public void setPointDirection(Point3i point, int xChange, int yChange, int zChange) {
        setPoint(point, ENCODING.encodeDirection(xChange, yChange, zChange));
    }

    /**
     * Sets all points in a list to point at the first point (the root point) in the list.
     *
     * @param points the list of points to set
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

    /**
     * Gets the encoded int buffer for a specific plane.
     *
     * @param z the z-coordinate of the plane
     * @return the {@link EncodedIntBuffer} for the specified plane
     */
    public EncodedIntBuffer getPixelsForPlane(int z) {
        return new EncodedIntBuffer(voxels.sliceBuffer(z), ENCODING);
    }

    /**
     * Checks if there are any temporary encoded values in the voxels.
     *
     * @return true if temporary values exist, false otherwise
     */
    public boolean hasTemporary() {
        int volumeXY = extent().areaXY();

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

    /**
     * Gets a list of points with temporary encoded values.
     *
     * @return a list of {@link Point3i} with temporary values
     */
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

    /**
     * Gets a set of all connected component IDs in the voxels.
     *
     * @return a set of integer IDs representing connected components
     */
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

    /**
     * Calculates the connected component ID for a given point and initial chain code.
     *
     * @param point the starting point
     * @param firstChainCode the initial chain code
     * @return the encoded connected component ID
     */
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

    /**
     * Checks if the given code represents a plateau.
     *
     * @param code the encoded value to check
     * @return true if the code represents a plateau, false otherwise
     */
    public boolean isPlateau(int code) {
        return code == WatershedEncoding.CODE_PLATEAU;
    }

    /**
     * Checks if the given code represents a minima.
     *
     * @param code the encoded value to check
     * @return true if the code represents a minima, false otherwise
     */
    public boolean isMinima(int code) {
        return code == WatershedEncoding.CODE_MINIMA;
    }

    /**
     * Checks if the given code represents a temporary value.
     *
     * @param code the encoded value to check
     * @return true if the code represents a temporary value, false otherwise
     */
    public boolean isTemporary(int code) {
        return code == WatershedEncoding.CODE_TEMPORARY;
    }

    /**
     * Checks if the given code represents an unvisited value.
     *
     * @param code the encoded value to check
     * @return true if the code represents an unvisited value, false otherwise
     */
    public boolean isUnvisited(int code) {
        return code == WatershedEncoding.CODE_UNVISITED;
    }

    /**
     * Checks if the given code represents a direction chain code.
     *
     * @param code the encoded value to check
     * @return true if the code represents a direction chain code, false otherwise
     */
    public boolean isDirectionChainCode(int code) {
        return ENCODING.isDirectionChainCode(code);
    }

    /**
     * Checks if the given code represents a connected component ID.
     *
     * @param code the encoded value to check
     * @return true if the code represents a connected component ID, false otherwise
     */
    public boolean isConnectedComponentIDCode(int code) {
        return ENCODING.isConnectedComponentIDCode(code);
    }

    /**
     * Gets the extent of the voxels.
     *
     * @return the {@link Extent} of the voxels
     */
    public Extent extent() {
        return voxels.extent();
    }
}