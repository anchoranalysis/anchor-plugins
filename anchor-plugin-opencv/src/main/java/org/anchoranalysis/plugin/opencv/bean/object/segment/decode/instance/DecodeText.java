/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.object.segment.decode.instance;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.object.properties.ObjectWithProperties;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.regionmap.RegionMapSingleton;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.dnn.Net;

/**
 * Extracts text from a RGB image by using the EAST deep neural network model
 *
 * <p>Each object-mask represented rotated-bounding box and is associated with a confidence score
 *
 * <p>Particular thanks to <a
 * href="https://www.pyimagesearch.com/2018/08/20/opencv-text-detection-east-text-detector/">Adrian
 * Rosebrock</a> whose tutorial was useful in applying this model
 *
 * @author Owen Feehan
 */
public class DecodeText extends DecodeInstanceSegmentation {
    
    private static final Scalar MEAN_SUBTRACTION_CONSTANTS = new Scalar(123.68, 116.78, 103.94);

    private static final String OUTPUT_SCORES = "feature_fusion/Conv_7/Sigmoid";
    private static final String OUTPUT_GEOMETRY = "feature_fusion/concat_3";
    
    // START BEAN PROPERTIES
    /** Proposed bounding boxes below this confidence interval are removed */
    @BeanField @Getter @Setter private double minConfidence = 0.5;
    // END BEAN PROPERTIES
    
    @Override
    public SegmentedObjects segmentMat(
            Mat mat,
            Dimensions dimensions,
            Extent unscaledSize,
            ScaleFactor scaleFactor,
            ConcurrentModelPool<Net> modelPool, Optional<List<String>> classLabels)
            throws Throwable {
        
        Dimensions dimensionsMat = dimensionsForMatrix(mat, dimensions.resolution());
        
        // Convert marks to object-masks
        SegmentedObjects objects = new SegmentedObjects(
                queueInference(modelPool, mat, unscaledSize, classLabels, dimensionsMat).stream());

        // TODO scale the marks up, not the object-masks for better resolution
        // TODO allow a reduction to occur here before scaling up
        
        // Scale each object-mask and extract as an object-collection
        return objects.scale(scaleFactor, unscaledSize);
    }
    
    @Override
    public List<String> expectedOutputs() {
        return Arrays.asList(OUTPUT_SCORES, OUTPUT_GEOMETRY);
    }
    
    @Override
    public Scalar meanSubtractionConstants() {
        return MEAN_SUBTRACTION_CONSTANTS;
    }

    @Override
    protected List<LabelledWithConfidence<ObjectMask>> decode(List<Mat> output, Extent unscaledSize,
            Optional<List<String>> classLabels, Dimensions inputDimensions) {
        return convertMarksToObject(EastMarkExtracter.decode(output, minConfidence),
                inputDimensions);
    }
        
    private static List<LabelledWithConfidence<ObjectMask>> convertMarksToObject(
            List<LabelledWithConfidence<Mark>> listMarks, Dimensions dim) {
        return FunctionalList.mapToList(
                listMarks, withConfidence -> convertToObject(withConfidence, dim));
    }

    private static Dimensions dimensionsForMatrix(Mat matrix, Optional<Resolution> resolution) {
        Extent extent = new Extent((int) matrix.size().width, (int) matrix.size().height);
        return new Dimensions(extent, resolution);
    }

    private static LabelledWithConfidence<ObjectMask> convertToObject(
            LabelledWithConfidence<Mark> withConfidence, Dimensions dimensions) {

        // TODO introduce map method on LabelledWithConfidence
        ObjectWithProperties object =
                withConfidence.getElement()
                        .deriveObject(
                                dimensions,
                                RegionMapSingleton.instance()
                                        .membershipWithFlagsForIndex(
                                                GlobalRegionIdentifiers.SUBMARK_INSIDE),
                                BinaryValuesByte.getDefault());
        return new LabelledWithConfidence<>(object.withoutProperties(), withConfidence.getConfidence(), withConfidence.getLabel());
    }
}
