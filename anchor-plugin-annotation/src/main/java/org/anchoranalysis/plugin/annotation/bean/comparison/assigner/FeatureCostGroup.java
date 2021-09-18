/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import org.anchoranalysis.annotation.io.assignment.OverlappingObjects;
import org.anchoranalysis.annotation.io.comparer.StatisticsToExport;
import org.anchoranalysis.math.arithmetic.RunningSumExtrema;
import org.anchoranalysis.plugin.annotation.counter.ImageCounterWithStatistics;

/**
 * A unique group that counts images and measures the success of an assignment for statistics.
 * 
 * <p>It is intended to be used together with {@link FeatureCostAssigner}.
 * 
 * @author Owen Feehan
 *
 */
class FeatureCostGroup implements ImageCounterWithStatistics<OverlappingObjects> {
    
    private int countMatched = 0;
    private int countUnmatchedLeft = 0;
    private int countUnmatchedRight = 0;
    private double sumOverlapRatio;

    private RunningSumExtrema percentLeftMatched = new RunningSumExtrema();
    private RunningSumExtrema percentRightMatched = new RunningSumExtrema();
    
    private GroupWithImageCount<OverlappingObjects> imageCount;

    public FeatureCostGroup(String identifier) {
        imageCount = new GroupWithImageCount<>(identifier);
    }

    @Override
    public void addAnnotatedImage(OverlappingObjects payload) {
        imageCount.addAnnotatedImage(payload);
        countMatched += payload.numberPaired();
        countUnmatchedLeft += payload.numberUnassigned(true);
        countUnmatchedRight += payload.numberUnassigned(false);

        sumOverlapRatio += payload.sumOverlapFromPaired();

        percentLeftMatched.add(payload.percentLeftAssigned());
        percentRightMatched.add(payload.percentRightAssigned());
    }
    
    @Override
    public StatisticsToExport comparison() {

        StatisticsToExport comparison = new StatisticsToExport();
        comparison.append( imageCount.comparison() );
        
        comparison.addDouble("percentMatchesInAnnotation", percentLeftMatched());
        comparison.addDouble("percentMatchesInResult", percentRightMatched());
        
        comparison.addInt("matches", countMatched);
        
        comparison.addInt("unmatchedAnnotation", countUnmatchedLeft);
        comparison.addInt("numItemsInAnnotation", leftSize());
        
        comparison.addInt("unmatchedResult", countUnmatchedRight);
        comparison.addInt("numberItemsInResult", rightSize());

        comparison.addDouble("meanOverlapRatio", meanOverlapRatio());

        comparison.addMeanExtrema("percentAnnotationMatched", percentLeftMatched);
        comparison.addMeanExtrema("percentResultMatched", percentRightMatched);

        return comparison;
    }

    private double percentLeftMatched() {
        return ((double) countMatched) * 100 / leftSize();
    }

    private double percentRightMatched() {
        return ((double) countMatched) * 100 / rightSize();
    }

    private int leftSize() {
        return countMatched + countUnmatchedLeft;
    }

    private int rightSize() {
        return countMatched + countUnmatchedRight;
    }

    private double meanOverlapRatio() {
        return sumOverlapRatio / countMatched;
    }

    @Override
    public void addUnannotatedImage() {
        imageCount.addUnannotatedImage();
    }
}
