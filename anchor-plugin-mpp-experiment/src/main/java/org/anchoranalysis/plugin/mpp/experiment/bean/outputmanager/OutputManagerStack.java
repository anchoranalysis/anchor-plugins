/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.outputmanager;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.output.bean.OutputManagerWithPrefixer;
import org.anchoranalysis.io.output.bean.allowed.AllOutputAllowed;
import org.anchoranalysis.io.output.bean.allowed.NoOutputAllowed;
import org.anchoranalysis.io.output.bean.allowed.OutputAllowed;
import org.anchoranalysis.mpp.io.output.StackOutputKeys;

public class OutputManagerStack extends OutputManagerWithPrefixer {

    // BEAN PROPERTIES
    /** What's allowed or not - highest level outputs */
    @BeanField @Getter @Setter private OutputAllowed outputEnabled = new AllOutputAllowed();

    /** What's allowed or not when outputting stacks */
    @BeanField @Getter @Setter
    private OutputAllowed stackCollectionOutputEnabled = new AllOutputAllowed();

    /** What's allowed or not when outputting configurations */
    @BeanField @Getter @Setter
    private OutputAllowed cfgCollectionOutputEnabled = new AllOutputAllowed();

    /** What's allowed or not when outputting object-collections */
    @BeanField @Getter @Setter private OutputAllowed objects = new AllOutputAllowed();

    /** What's allowed or not when outputting histograms */
    @BeanField @Getter @Setter
    private OutputAllowed histogramCollectionOutputEnabled = new AllOutputAllowed();
    // END BEAN PROPERTIES

    @Override
    public OutputAllowed outputAllowedSecondLevel(String key) {
        switch (key) {
            case StackOutputKeys.STACK:
                return getStackCollectionOutputEnabled();
            case StackOutputKeys.CFG:
                return getCfgCollectionOutputEnabled();
            case StackOutputKeys.HISTOGRAM:
                return getHistogramCollectionOutputEnabled();
            case StackOutputKeys.OBJECTS:
                return getObjects();
            default:
                return new NoOutputAllowed();
        }
    }

    @Override
    public boolean isOutputAllowed(String outputName) {
        return outputEnabled.isOutputAllowed(outputName);
    }
}
