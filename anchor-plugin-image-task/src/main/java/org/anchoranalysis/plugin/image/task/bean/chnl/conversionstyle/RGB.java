/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.chnl.conversionstyle;

import java.util.Set;
import java.util.function.BiConsumer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.image.task.chnl.convert.ChnlGetterForTimepoint;

public class RGB extends ChnlConversionStyle {

    // START BEAN PROPERTIES
    /**
     * If a channel doesn't match an RGB pattern, this conversion-style can be used instead.
     *
     * <p>If unset, an error is instead thrown in this circumstances
     */
    @BeanField @OptionalBean private ChnlConversionStyle fallback;
    // END BEAN PROPERTIES

    @Override
    public void convert(
            Set<String> chnlNames,
            ChnlGetterForTimepoint chnlGetter,
            BiConsumer<String, Stack> stacksOut,
            Logger logger)
            throws AnchorIOException {

        if (!chnlNamesAreRGB(chnlNames)) {
            // Not compatable with RGB
            if (fallback != null) {
                fallback.convert(chnlNames, chnlGetter, stacksOut, logger);
                return;
            } else {
                throw new AnchorIOException("Cannot convert as its channels do not look like RGB");
            }
        }

        try {
            Stack stack = createRGBStack(chnlGetter, logger.messageLogger());

            // The name is blank as there is a single channel
            stacksOut.accept("", stack);
        } catch (CreateException e) {
            throw new AnchorIOException("Incorrect image size", e);
        }
    }

    private static Stack createRGBStack(ChnlGetterForTimepoint chnlGetter, MessageLogger logger)
            throws CreateException {

        Stack stackRearranged = new Stack();

        addChnlOrBlank("red", chnlGetter, stackRearranged, logger);
        addChnlOrBlank("green", chnlGetter, stackRearranged, logger);
        addChnlOrBlank("blue", chnlGetter, stackRearranged, logger);

        return stackRearranged;
    }

    private static void addChnlOrBlank(
            String chnlName,
            ChnlGetterForTimepoint chnlGetter,
            Stack stackRearranged,
            MessageLogger logger)
            throws CreateException {
        try {
            if (chnlGetter.hasChnl(chnlName)) {
                stackRearranged.addChnl(chnlGetter.getChnl(chnlName));
            } else {
                logger.logFormatted(String.format("Adding a blank channel for %s", chnlName));
                stackRearranged.addBlankChnl();
            }
        } catch (IncorrectImageSizeException
                | OperationFailedException
                | GetOperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static boolean chnlNamesAreRGB(Set<String> chnlNames) {
        if (chnlNames.size() > 3) {
            return false;
        }

        for (String key : chnlNames) {
            // If a key doesn't match one of the expected red-green-blue names
            if (!(key.equals("red") || key.equals("green") || key.equals("blue"))) {
                return false;
            }
        }

        return true;
    }

    public ChnlConversionStyle getFallback() {
        return fallback;
    }

    public void setFallback(ChnlConversionStyle fallback) {
        this.fallback = fallback;
    }
}
