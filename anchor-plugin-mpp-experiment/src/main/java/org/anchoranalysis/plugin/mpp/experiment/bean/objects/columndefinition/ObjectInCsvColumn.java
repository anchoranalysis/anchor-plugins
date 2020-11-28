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

import lombok.Getter;
import lombok.Value;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionRTree;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * A column (or portion of a column) in a CSV file, with each row describing a point for a unique
 * voxel that lies within an object.
 *
 * @author Owen Feehan
 */
@Value
class ObjectInCsvColumn {

    /** The index of a column describing a unique-pixel within an object. */
    @Getter private final int indexColumnUniqueVoxel;

    /**
     * If there are two points encoded in the <i>unique-pixel</i> column, rather than one, then
     * whether to select the first or the second.
     */
    @Getter private final boolean first;

    /**
     * A string that describes an object-mask referred to in a row in a CSV file.
     *
     * @param csvRow the csv-row describing the object
     * @return a string describing the object's point (as above) and its exact number-of-voxels
     */
    public String describeObject(CSVRow csvRow) {
        return ArrayExtracter.getAsPoint(csvRow.getLine(), indexColumnUniqueVoxel, first)
                .toString();
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
        return findObjectThatMatches(
                allObjects,
                ArrayExtracter.getAsPoint(csvRow.getLine(), indexColumnUniqueVoxel, first));
    }

    /**
     * Finds an object that contains a particular point.
     *
     * @param objectsToSearch the objects to search for a particular object
     * @param point a point the particular object must contain
     * @return the object that contains the point
     * @throws OperationFailedException if no matching object can be found, or more than one is
     *     found
     */
    private static ObjectMask findObjectThatMatches(
            ObjectCollectionRTree objectsToSearch, Point3i point) throws OperationFailedException {

        ObjectCollection objects = objectsToSearch.contains(point);

        if (objects.size() > 1) {
            throw new OperationFailedException("More than object lies on point: " + point);
        }

        if (objects.size() == 0) {
            throw new OperationFailedException(
                    String.format("Cannot find matching object for %s", point.toString()));
        }

        return objects.get(0);
    }
}
