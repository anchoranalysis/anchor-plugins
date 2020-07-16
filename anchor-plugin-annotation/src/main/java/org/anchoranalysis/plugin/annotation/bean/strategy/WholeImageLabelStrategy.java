/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.wholeimage.WholeImageLabelAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.gui.annotation.bean.label.AnnotationLabel;
import org.anchoranalysis.gui.annotation.bean.label.GroupedAnnotationLabels;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.error.AnchorIOException;

public class WholeImageLabelStrategy extends SingleFilePathGeneratorStrategy {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<AnnotationLabel> labels;

    @BeanField @Getter @Setter private int weightWidthDescription;
    // END BEAN PROPERTIES

    @Override
    public Optional<String> annotationLabelFor(ProvidesStackInput item) throws AnchorIOException {
        return ReadAnnotationFromFile.readCheckExists(annotationPathFor(item))
                .map(WholeImageLabelAnnotation::getLabel);
    }

    // This is actually called twice during a typically opening of an annotation
    // But overhead is minor (assuming not very many labels)
    public GroupedAnnotationLabels groupedLabels() {
        return new GroupedAnnotationLabels(labels);
    }

    @Override
    public int weightWidthDescription() {
        return weightWidthDescription;
    }
}
