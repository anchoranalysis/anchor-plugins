/* (C)2020 */
package org.anchoranalysis.plugin.image.task.imagefeature.calculator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.io.input.StackInputInitParamsCreator;
import org.anchoranalysis.io.output.bound.BoundIOContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureCalculatorRepeated {

    public static NRGStackWithParams extractStack(
            ProvidesStackInput inputObject, StackProvider nrgStackProvider, BoundIOContext context)
            throws OperationFailedException {
        ImageInitParams paramsInit =
                StackInputInitParamsCreator.createInitParams(inputObject, context);
        return ExtractFromProvider.extractStack(nrgStackProvider, paramsInit, context.getLogger());
    }
}
