/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoCollection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class CoefficientOfVarianceFromAll extends FeatureAllMemo {

    // START BEAN PROPERTIES
    @BeanField private Feature<FeatureInputSingleMemo> item;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputAllMemo> input) throws FeatureCalcException {

        MemoCollection memoMarks = input.get().getPxlPartMemo();

        if (memoMarks.size() == 0) {
            return 0.0;
        }

        return calcStatistic(input, memoMarks);
    }

    private double calcStatistic(SessionInput<FeatureInputAllMemo> input, MemoCollection memoMarks)
            throws FeatureCalcException {

        double[] vals = new double[memoMarks.size()];

        double mean = calcForEachItem(input, memoMarks, vals);

        if (mean == 0.0) {
            return Double.POSITIVE_INFINITY;
        }

        return stdDev(vals, mean) / mean;
    }

    /** Calculates the feature on each mark separately, populating vals, and returns the mean */
    private double calcForEachItem(
            SessionInput<FeatureInputAllMemo> input, MemoCollection memoMarks, double[] vals)
            throws FeatureCalcException {

        double sum = 0.0;

        for (int i = 0; i < memoMarks.size(); i++) {

            final int index = i;

            double v =
                    input.forChild()
                            .calc(
                                    item,
                                    new CalculateDeriveSingleMemoInput(index),
                                    new ChildCacheName(CoefficientOfVarianceFromAll.class, i));

            vals[i] = v;
            sum += v;
        }

        return sum / memoMarks.size();
    }

    private static double stdDev(double[] vals, double mean) {
        double sumSqDiff = 0.0;
        for (int i = 0; i < vals.length; i++) {
            double diff = vals[i] - mean;
            sumSqDiff += Math.pow(diff, 2.0);
        }

        return Math.sqrt(sumSqDiff);
    }

    public Feature<FeatureInputSingleMemo> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputSingleMemo> item) {
        this.item = item;
    }
}
