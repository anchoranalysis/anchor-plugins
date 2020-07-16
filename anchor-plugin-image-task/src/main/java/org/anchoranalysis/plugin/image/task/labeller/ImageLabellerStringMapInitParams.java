/* (C)2020 */
package org.anchoranalysis.plugin.image.task.labeller;

import java.util.Map;

/**
 * @author Owen Feehan
 * @param <T> type of the initParams contained within (delegate
 */
public class ImageLabellerStringMapInitParams<T> {

    private T initParams;
    private Map<String, String> map;

    public ImageLabellerStringMapInitParams(Map<String, String> map, T initParams) {
        super();
        this.map = map;
        this.initParams = initParams;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public T getInitParams() {
        return initParams;
    }
}
