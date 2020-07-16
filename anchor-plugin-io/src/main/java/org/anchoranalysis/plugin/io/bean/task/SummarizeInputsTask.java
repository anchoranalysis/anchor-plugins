/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.task;

import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.io.input.InputFromManager;

public class SummarizeInputsTask<T extends InputFromManager> extends SummarizeTask<T, T> {

    @Override
    protected T extractObjectForSummary(T input) {
        return input;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(InputFromManager.class);
    }
}
