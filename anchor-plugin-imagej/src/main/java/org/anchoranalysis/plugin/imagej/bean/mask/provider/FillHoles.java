/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.mask.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.UnitValueException;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.bean.unitvalue.extent.UnitValueExtent;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.mask.MaskFromObjects;
import org.anchoranalysis.image.core.mask.combine.MaskAnd;
import org.anchoranalysis.image.core.mask.combine.MaskOr;
import org.anchoranalysis.image.voxel.binary.connected.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.imagej.mask.ApplyImageJMorphologicalOperation;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Fills holes in a mask, with options to skip holes touching the border or exceeding a maximum
 * volume.
 *
 * <p>This class extends {@link MaskProviderUnary} to provide functionality for filling holes in
 * binary masks.
 */
public class FillHoles extends MaskProviderUnary {

    /**
     * The maximum volume of a hole to be filled. If null, no maximum volume constraint is applied.
     */
    @BeanField @OptionalBean @Getter @Setter private UnitValueExtent maxVolume = null;

    /** If true, holes that touch the border of the mask are not filled. Default value is true. */
    @BeanField @Getter @Setter private boolean skipAtBorder = true;

    @Override
    public Mask createFromMask(Mask mask) throws ProvisionFailedException {

        Mask maskDuplicated = fillMask(mask);

        try {
            return fillHoles(
                    filterObjectsFromMask(maskDuplicated),
                    mask,
                    mask.dimensions(),
                    BinaryValuesInt.getDefault());
        } catch (UnitValueException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Fills holes in the input mask.
     *
     * @param mask the input mask
     * @return a new mask with holes filled
     * @throws ProvisionFailedException if the operation fails
     */
    private Mask fillMask(Mask mask) throws ProvisionFailedException {
        Mask maskInverted = new Mask(mask.channel(), mask.binaryValuesInt().createInverted());

        Mask maskDuplicated = mask.duplicate();

        try {
            ApplyImageJMorphologicalOperation.fill(maskDuplicated.binaryVoxels());
        } catch (OperationFailedException e1) {
            throw new ProvisionFailedException(e1);
        }

        MaskAnd.apply(maskDuplicated, maskInverted);

        return maskDuplicated;
    }

    /**
     * Filters objects from the mask based on the class's criteria.
     *
     * @param mask the input mask
     * @return a collection of filtered objects
     * @throws UnitValueException if there's an issue with unit conversion
     */
    private ObjectCollection filterObjectsFromMask(Mask mask) throws UnitValueException {

        ObjectsFromConnectedComponentsFactory objectCreator =
                new ObjectsFromConnectedComponentsFactory();

        return filterObjects(
                objectCreator.createUnsignedByte(mask.binaryVoxels()), mask.dimensions());
    }

    /**
     * Filters objects based on the class's criteria.
     *
     * @param objects the collection of objects to filter
     * @param dimensions the dimensions of the image
     * @return a filtered collection of objects
     * @throws UnitValueException if there's an issue with unit conversion
     */
    private ObjectCollection filterObjects(ObjectCollection objects, Dimensions dimensions)
            throws UnitValueException {

        final double maxVolumeResolved = determineMaxVolume(dimensions);

        return objects.stream()
                .filter(
                        objectMask ->
                                includeObject(objectMask, dimensions.extent(), maxVolumeResolved));
    }

    /**
     * Determines the maximum volume in voxels based on the maxVolume field.
     *
     * @param dimensions the dimensions of the image
     * @return the maximum volume in voxels
     * @throws UnitValueException if there's an issue with unit conversion
     */
    private double determineMaxVolume(Dimensions dimensions) throws UnitValueException {
        if (maxVolume != null) {
            return maxVolume.resolveToVoxels(dimensions.unitConvert());
        } else {
            return 0; // Arbitrary, as it will be ignored anyway
        }
    }

    /**
     * Determines whether an object should be included based on the class's criteria.
     *
     * @param object the object to check
     * @param extent the extent of the image
     * @param maxVolumeResolved the maximum volume in voxels
     * @return true if the object should be included, false otherwise
     */
    private boolean includeObject(ObjectMask object, Extent extent, double maxVolumeResolved) {
        // It's not allowed touch the border
        if (skipAtBorder && object.boundingBox().atBorderXY(extent)) {
            return false;
        }

        // Volume check
        return maxVolume == null || object.numberVoxelsOn() <= maxVolumeResolved;
    }

    /**
     * Creates a mask from filled objects and combines it with the source mask.
     *
     * @param filled the collection of filled objects
     * @param source the source mask
     * @param dimensions the dimensions of the image
     * @param binaryValuesOut the binary values for the output mask
     * @return the combined mask
     */
    private static Mask fillHoles(
            ObjectCollection filled,
            Mask source,
            Dimensions dimensions,
            BinaryValuesInt binaryValuesOut) {
        Mask maskFromObjects =
                MaskFromObjects.createFromObjects(filled, dimensions, binaryValuesOut);
        MaskOr.apply(maskFromObjects, source);
        return maskFromObjects;
    }
}
