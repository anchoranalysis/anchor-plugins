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
