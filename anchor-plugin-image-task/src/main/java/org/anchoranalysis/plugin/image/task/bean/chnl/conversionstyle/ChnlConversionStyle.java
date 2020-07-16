/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.chnl.conversionstyle;

import java.util.Set;
import java.util.function.BiConsumer;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.image.task.chnl.convert.ChnlGetterForTimepoint;

public abstract class ChnlConversionStyle extends AnchorBean<ChnlConversionStyle> {

    public abstract void convert(
            Set<String> chnlNames,
            ChnlGetterForTimepoint chnlGetter,
            BiConsumer<String, Stack> stacksOut,
            Logger logger)
            throws AnchorIOException;
}
