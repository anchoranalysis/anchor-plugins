package org.anchoranalysis.plugin.io.bean.grouper;

import org.anchoranalysis.io.input.bean.grouper.FromDerivePath;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.plugin.io.bean.path.derive.CollapseFileName;

/**
 * Splits an identifier into elements by the directory separator, and removes the final element.
 *
 * <p>If there is only one element (i.e. directory separator), it is left unchanged.
 *
 * @author Owen Feehan
 */
public class RemoveLastElement extends FromDerivePath {

    private static final CollapseFileName DELEGATE = new CollapseFileName();

    @Override
    protected DerivePath selectDerivePath() {
        return DELEGATE;
    }
}
