/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition;

import com.google.common.base.Preconditions;
import lombok.Value;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionRTree;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.anchoranalysis.spatial.point.Point3i;

@Value
class ObjectInCsvRowIndices {

    /** the index in the CSV row of the exact number of voxels of the object */
    private final int numberVoxels;

    /**
     * three-element array of the indices in the CSV row of the X, Y, Z components of a point that
     * must exist within the object.
     */
    private final int[] point;

    /**
     * A string that describes an object-mask referred to in a row in a CSV file.
     *
     * @param csvRow the csv-row describing the object
     * @return a string describing the object's point (as above) and its exact number-of-voxels
     */
    public String describeObject(CSVRow csvRow) {
        Preconditions.checkArgument(point.length == 3);

        String[] line = csvRow.getLine();

        return String.format(
                "%s (numVoxels=%d)",
                ArrayExtracter.getAsPoint(line, point),
                ArrayExtracter.getAsInt(line, numberVoxels));
    }

    /**
     * Finds an object that matches a particular row in a CSV file describing an object
     *
     * @param allObjects the objects to search for a particular object
     * @param csvRow the csv-row describing the object
     * @return the first object (if found) that contains the point and has the exact number of
     *     voxels required
     * @throws OperationFailedException if no matching object can be found
     */
    public ObjectMask findObjectFromCSVRow(ObjectCollectionRTree allObjects, CSVRow csvRow)
            throws OperationFailedException {
        Preconditions.checkArgument(point.length == 3);

        String[] line = csvRow.getLine();

        return findObjectThatMatches(
                allObjects,
                ArrayExtracter.getAsPoint(line, point),
                ArrayExtracter.getAsInt(line, numberVoxels));
    }

    /**
     * Finds an object that contains a particular point, and has the exact number of voxels
     * indicated
     *
     * @param objectsToSearch the objects to search for a particular object
     * @param point a point the particular object must contain
     * @param numberVoxels the exact number of voxels the particular object must have
     * @return the first object (if found) that contains the point and has the exact number of
     *     voxels required
     * @throws OperationFailedException if no matching object can be found
     */
    private static ObjectMask findObjectThatMatches(
            ObjectCollectionRTree objectsToSearch, Point3i point, int numberVoxels)
            throws OperationFailedException {

        ObjectCollection objects = objectsToSearch.contains(point);

        for (ObjectMask objectMask : objects) {
            if (objectMask.numberVoxelsOn() == numberVoxels) {
                return objectMask;
            }
        }

        throw new OperationFailedException(
                String.format(
                        "Cannot find matching object for %s (with numVoxels=%d)",
                        point.toString(), numberVoxels));
    }
}
