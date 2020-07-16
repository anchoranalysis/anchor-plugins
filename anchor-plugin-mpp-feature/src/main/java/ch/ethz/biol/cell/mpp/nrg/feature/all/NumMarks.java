/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class NumMarks extends FeatureAllMemo {

    @Override
    public double calc(SessionInput<FeatureInputAllMemo> params) throws FeatureCalcException {
        return params.get().getPxlPartMemo().size();
    }
}
