/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.MarkEvaluator;
import org.anchoranalysis.annotation.io.bean.comparer.MultipleComparer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;

/** Annotates each image with a mark */
public class MarkProposerStrategy extends SingleFilePathGeneratorStrategy {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String markProposerName;

    @BeanField @Getter @Setter private String pointsFitterName;

    @BeanField @Getter @Setter private String markEvaluatorName;

    @BeanField @OptionalBean @Getter @Setter private FilePathGenerator defaultCfgFilePathGenerator;

    @BeanField @OptionalBean @Getter @Setter
    private FilePathGenerator keyValueParamsFilePathGenerator;

    @BeanField @OptionalBean @Getter @Setter
    private List<FilePathGenerator> listDisplayRasters = new ArrayList<>();

    @BeanField @OptionalBean @Getter @Setter private MultipleComparer multipleComparer;

    /** If-defined, mark-evaluator that is added to the GUI to support this annotation */
    @BeanField @OptionalBean @Getter @Setter private MarkEvaluator markEvaluator;
    // END BEAN PROPERTIES

    public Optional<FilePathGenerator> paramsFilePathGenerator() {
        return Optional.ofNullable(keyValueParamsFilePathGenerator);
    }

    public Optional<FilePathGenerator> cfgFilePathGenerator() {
        return Optional.ofNullable(defaultCfgFilePathGenerator);
    }

    @Override
    public int weightWidthDescription() {
        return 1;
    }

    @Override
    public Optional<String> annotationLabelFor(ProvidesStackInput item) {
        return Optional.empty();
    }
}
