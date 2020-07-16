/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.StringMap;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.labeller.ImageLabellerStringMapInitParams;

/**
 * Maps one set of labels to another
 *
 * @author Owen Feehan
 * @param T the init-param-type of filter that is the delegate
 */
public class ImageLabellerStringMap<T> extends ImageLabeller<ImageLabellerStringMapInitParams<T>> {

    // START BEAN PROPERTIES
    @BeanField private ImageLabeller<T> filter;

    @BeanField private StringMap map;
    // END BEAN PROPERTIES

    @Override
    public ImageLabellerStringMapInitParams<T> init(Path pathForBinding) throws InitException {
        return new ImageLabellerStringMapInitParams<>(map.create(), filter.init(pathForBinding));
    }

    @Override
    public Set<String> allLabels(ImageLabellerStringMapInitParams<T> params) {
        return new HashSet<>(params.getMap().values());
    }

    @Override
    public String labelFor(
            ImageLabellerStringMapInitParams<T> sharedState,
            ProvidesStackInput input,
            BoundIOContext context)
            throws OperationFailedException {
        String firstId = filter.labelFor(sharedState.getInitParams(), input, context);
        return sharedState.getMap().get(firstId);
    }

    public StringMap getMap() {
        return map;
    }

    public void setMap(StringMap map) {
        this.map = map;
    }

    public ImageLabeller<T> getFilter() {
        return filter;
    }

    public void setFilter(ImageLabeller<T> filter) {
        this.filter = filter;
    }
}
