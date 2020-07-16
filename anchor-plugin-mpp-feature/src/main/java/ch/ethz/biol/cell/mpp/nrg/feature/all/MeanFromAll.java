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

public class MeanFromAll extends FeatureAllMemo {

    // START BEAN PROPERTIES
    @BeanField private Feature<FeatureInputSingleMemo> item;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputAllMemo> input) throws FeatureCalcException {

        MemoCollection memo = input.get().getPxlPartMemo();

        if (memo.size() == 0) {
            return 0.0;
        }

        double sum = 0.0;

        for (int i = 0; i < memo.size(); i++) {

            sum +=
                    input.forChild()
                            .calc(
                                    item,
                                    new CalculateDeriveSingleMemoInput(i),
                                    new ChildCacheName(MeanFromAll.class, i));
        }

        return sum / memo.size();
    }

    public Feature<FeatureInputSingleMemo> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputSingleMemo> item) {
        this.item = item;
    }
}
