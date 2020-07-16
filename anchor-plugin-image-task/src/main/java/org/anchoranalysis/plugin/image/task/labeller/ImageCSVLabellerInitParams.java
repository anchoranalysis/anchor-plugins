/* (C)2020 */
package org.anchoranalysis.plugin.image.task.labeller;

import org.anchoranalysis.image.experiment.label.FileLabelMap;

public class ImageCSVLabellerInitParams {

    private FileLabelMap<String> labelMap;

    public ImageCSVLabellerInitParams(FileLabelMap<String> labelMap) {
        super();
        assert (labelMap != null);
        this.labelMap = labelMap;
    }

    public FileLabelMap<String> getLabelMap() {
        return labelMap;
    }
}
