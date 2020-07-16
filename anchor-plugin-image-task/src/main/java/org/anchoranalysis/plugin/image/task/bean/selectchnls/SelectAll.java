/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.selectchnls;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/**
 * Selects all possible channels from all possible stacks
 *
 * <p>If a stack has a single-channel, it it uses this name as an output If a stack has multiple
 * channels, this name is used but suffixed with a number of each channel (00, 01 etc.)
 *
 * @author Owen Feehan
 */
public class SelectAll extends SelectChnlsFromStacks {

    @Override
    public List<NamedChnl> selectChnls(ChannelSource source, boolean checkType)
            throws OperationFailedException {

        List<NamedChnl> out = new ArrayList<>();

        for (String key : source.getStackStore().keys()) {

            out.addAll(extractAllChnls(source, key, checkType));
        }

        return out;
    }

    private List<NamedChnl> extractAllChnls(
            ChannelSource source, String stackName, boolean checkType)
            throws OperationFailedException {

        try {
            // We make a single histogram
            Stack stack = source.getStackStore().getException(stackName);

            List<NamedChnl> out = new ArrayList<>();
            for (int i = 0; i < stack.getNumChnl(); i++) {

                String outputName = stackName + createSuffix(i, stack.getNumChnl() > 1);

                out.add(new NamedChnl(outputName, source.extractChnl(stack, checkType, i)));
            }
            return out;

        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(e.summarize());
        }
    }

    private static String createSuffix(int index, boolean hasMultipleChnls) {
        if (hasMultipleChnls) {
            return String.format("%02d", index);
        } else {
            return "";
        }
    }
}
