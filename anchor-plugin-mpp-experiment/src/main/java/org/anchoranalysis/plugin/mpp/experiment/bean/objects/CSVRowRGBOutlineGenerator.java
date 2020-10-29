package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.object.output.rgb.DrawCroppedObjectsGenerator;
import org.anchoranalysis.image.io.stack.output.generator.RasterGeneratorDelegateToRaster;
import org.anchoranalysis.image.voxel.object.ObjectCollectionRTree;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;

class CSVRowRGBOutlineGenerator
        extends RasterGeneratorDelegateToRaster<ObjectCollectionWithProperties, CSVRow> {

    /** All objects associated with the image. */
    private ObjectCollectionRTree allObjects;

    public CSVRowRGBOutlineGenerator(
            DrawCroppedObjectsGenerator drawCroppedGenerator,
            ObjectCollectionRTree allObjects
    ) {
        super(drawCroppedGenerator);
        this.allObjects = allObjects;
    }

    @Override
    protected ObjectCollectionWithProperties convertBeforeAssign(CSVRow element)
            throws OperationFailedException {
        return element.findObjectsMatchingRow(allObjects);
    }

    @Override
    protected Stack convertBeforeTransform(Stack stack) {
        return stack;
    }
}