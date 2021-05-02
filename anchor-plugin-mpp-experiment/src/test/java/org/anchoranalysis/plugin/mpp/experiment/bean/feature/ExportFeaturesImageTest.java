package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.junit.jupiter.api.Test;

class ExportFeaturesImageTest
        extends ExportFeaturesTestBase<ProvidesStackInput, FeatureInputStack, TaskFixtureStack> {

    private static final String EXPECTED_OUTPUT_SUBDIRECTORY = "stack";

    public static final String OUTPUT_DIRECTORY_SIMPLE = "simple/";

    ExportFeaturesImageTest() {
        super(EXPECTED_OUTPUT_SUBDIRECTORY, false, loader -> new TaskFixtureStack(loader));
    }

    @Test
    void testSimple() throws OperationFailedException {
        testOnTask(
                OUTPUT_DIRECTORY_SIMPLE, fixture -> {} // Change nothing
                );
    }

    @Override
    protected ProvidesStackInput createInput(EnergyStackWithoutParams stack) {
        return MultiInputFixture.createInput(stack);
    }
}
