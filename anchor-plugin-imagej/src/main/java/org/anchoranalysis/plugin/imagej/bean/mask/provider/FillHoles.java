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

public class FillHoles extends MaskProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private UnitValueExtent maxVolume = null;

    /** If true we do not fill any objects that touch the border */
    @BeanField @Getter @Setter private boolean skipAtBorder = true;
    // END BEAN PROPERTIES

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

    private ObjectCollection filterObjectsFromMask(Mask mask) throws UnitValueException {

        ObjectsFromConnectedComponentsFactory objectCreator =
                new ObjectsFromConnectedComponentsFactory();

        return filterObjects(
                objectCreator.createUnsignedByte(mask.binaryVoxels()), mask.dimensions());
    }

    private ObjectCollection filterObjects(ObjectCollection objects, Dimensions dimensions)
            throws UnitValueException {

        final double maxVolumeResolved = determineMaxVolume(dimensions);

        return objects.stream()
                .filter(
                        objectMask ->
                                includeObject(objectMask, dimensions.extent(), maxVolumeResolved));
    }

    private double determineMaxVolume(Dimensions dimensions) throws UnitValueException {
        if (maxVolume != null) {
            return maxVolume.resolveToVoxels(dimensions.unitConvert());
        } else {
            return 0; // Arbitrary, as it will be ignored anyway
        }
    }

    private boolean includeObject(ObjectMask object, Extent extent, double maxVolumeResolved) {
        // It's not allowed touch the border
        if (skipAtBorder && object.boundingBox().atBorderXY(extent)) {
            return false;
        }

        // Volume check
        return maxVolume == null || object.numberVoxelsOn() <= maxVolumeResolved;
    }

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
