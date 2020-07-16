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
/* (C)2020 */
package org.anchoranalysis.plugin.annotation.comparison;

import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.annotation.io.assignment.AssignmentOverlapFromPairs;
import org.anchoranalysis.core.text.TypedValue;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AnnotationGroupObject extends AnnotationGroup<AssignmentOverlapFromPairs> {

    private int countMatched = 0;
    private int countUnmatchedLeft = 0;
    private int countUnmatchedRight = 0;
    private double sumOverlapRatio;

    private RangeSum percentLeftMatched = new RangeSum();
    private RangeSum percentRightMatched = new RangeSum();

    public AnnotationGroupObject(String identifier) {
        super(identifier);
    }

    @Override
    public void addAcceptedAnnotation(AssignmentOverlapFromPairs assignment) {
        super.addAcceptedAnnotation(assignment);
        countMatched += assignment.numPaired();
        countUnmatchedLeft += assignment.numUnassigned(true);
        countUnmatchedRight += assignment.numUnassigned(false);

        sumOverlapRatio += assignment.sumOverlapFromPaired();

        percentLeftMatched.add(assignment.percentLeftMatched());
        percentRightMatched.add(assignment.percentRightMatched());
    }

    @Override
    public List<String> createHeaders() {
        List<String> headerNames = super.createHeaders();
        headerNames.addAll(
                Arrays.asList(
                        "percentMatchesInAnnotation",
                        "percentMatchesInResult",
                        "matches",
                        "unmatchedAnnotation",
                        "numItemsInAnnotation",
                        "unmatchedResult",
                        "numItemsInResult",
                        "meanOverlapRatio",
                        "meanPercentAnnotationMatched",
                        "minPercentAnnotationMatched",
                        "maxPercentAnnotationMatched",
                        "meanPercentResultMatched",
                        "minPercentResultMatched",
                        "maxPercentResultMatched"));
        return headerNames;
    }

    @Override
    public List<TypedValue> createValues() {
        List<TypedValue> rowElements = super.createValues();

        addDouble(rowElements, percentLeftMatched());
        addDouble(rowElements, percentRightMatched());

        rowElements.add(new TypedValue(numMatched()));

        rowElements.add(new TypedValue(numUnassignedLeft()));
        rowElements.add(new TypedValue(leftSize()));

        rowElements.add(new TypedValue(numUnassignedRight()));
        rowElements.add(new TypedValue(rightSize()));

        addDouble(rowElements, meanOverlapRatio());

        addRangeSum(rowElements, getPercentLeftMatched());
        addRangeSum(rowElements, getPercentRightMatched());

        return rowElements;
    }

    private static void addRangeSum(List<TypedValue> rowElements, RangeSum rangeSum) {
        addDouble(rowElements, rangeSum.mean());
        addDouble(rowElements, rangeSum.min());
        addDouble(rowElements, rangeSum.max());
    }

    private static void addDouble(List<TypedValue> rowElements, double val) {
        rowElements.add(new TypedValue(val, 2));
    }

    private double percentLeftMatched() {
        return ((double) countMatched) * 100 / leftSize();
    }

    private double percentRightMatched() {
        return ((double) countMatched) * 100 / rightSize();
    }

    private int numMatched() {
        return countMatched;
    }

    private int numUnassignedLeft() {
        return countUnmatchedLeft;
    }

    private int numUnassignedRight() {
        return countUnmatchedRight;
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

    private RangeSum getPercentLeftMatched() {
        return percentLeftMatched;
    }

    private RangeSum getPercentRightMatched() {
        return percentRightMatched;
    }
}
