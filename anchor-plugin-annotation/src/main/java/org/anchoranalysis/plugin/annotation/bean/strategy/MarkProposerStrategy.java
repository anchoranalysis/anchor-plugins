/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.MarkEvaluator;
import org.anchoranalysis.annotation.io.bean.comparer.MultipleComparer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;

/** Annotates each image with a mark */
public class MarkProposerStrategy extends SingleFilePathGeneratorStrategy {

    // START BEAN PROPERTIES
    @BeanField private String markProposerName;

    @BeanField private String pointsFitterName;

    @BeanField private String markEvaluatorName;

    @BeanField @OptionalBean private FilePathGenerator defaultCfgFilePathGenerator;

    @BeanField @OptionalBean private FilePathGenerator keyValueParamsFilePathGenerator;

    @BeanField @OptionalBean private List<FilePathGenerator> listDisplayRasters = new ArrayList<>();

    @BeanField @OptionalBean private MultipleComparer multipleComparer;

    /** If-defined, mark-evaluator that is added to the GUI to support this annotation */
    @BeanField @OptionalBean private MarkEvaluator markEvaluator;
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

    public MarkEvaluator getMarkEvaluator() {
        return markEvaluator;
    }

    public void setMarkEvaluator(MarkEvaluator markEvaluator) {
        this.markEvaluator = markEvaluator;
    }

    public String getMarkProposerName() {
        return markProposerName;
    }

    public void setMarkProposerName(String markProposerName) {
        this.markProposerName = markProposerName;
    }

    public String getPointsFitterName() {
        return pointsFitterName;
    }

    public void setPointsFitterName(String pointsFitterName) {
        this.pointsFitterName = pointsFitterName;
    }

    public String getMarkEvaluatorName() {
        return markEvaluatorName;
    }

    public void setMarkEvaluatorName(String markEvaluatorName) {
        this.markEvaluatorName = markEvaluatorName;
    }

    public FilePathGenerator getDefaultCfgFilePathGenerator() {
        return defaultCfgFilePathGenerator;
    }

    public void setDefaultCfgFilePathGenerator(FilePathGenerator defaultCfgFilePathGenerator) {
        this.defaultCfgFilePathGenerator = defaultCfgFilePathGenerator;
    }

    public FilePathGenerator getKeyValueParamsFilePathGenerator() {
        return keyValueParamsFilePathGenerator;
    }

    public void setKeyValueParamsFilePathGenerator(
            FilePathGenerator keyValueParamsFilePathGenerator) {
        this.keyValueParamsFilePathGenerator = keyValueParamsFilePathGenerator;
    }

    public List<FilePathGenerator> getListDisplayRasters() {
        return listDisplayRasters;
    }

    public void setListDisplayRasters(List<FilePathGenerator> listDisplayRasters) {
        this.listDisplayRasters = listDisplayRasters;
    }

    public MultipleComparer getMultipleComparer() {
        return multipleComparer;
    }

    public void setMultipleComparer(MultipleComparer multipleComparer) {
        this.multipleComparer = multipleComparer;
    }
}
