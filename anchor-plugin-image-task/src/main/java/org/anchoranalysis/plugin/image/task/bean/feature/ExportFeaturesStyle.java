package org.anchoranalysis.plugin.image.task.bean.feature;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Visual style for how features are exported.
 * 
 * @author Owen Feehan
 *
 */
public class ExportFeaturesStyle extends AnchorBean<ExportFeaturesStyle> {

    /**
     * When true, columns containing all {@link Double#NaN} values are removed before outputting.
     * 
     * <p>When false, all columns are included.
     */
    @BeanField @Getter @Setter boolean removeNaNColumns = true;
    
    /**
     * When true, feature-values are shown as visually compressed as possible, including suppressing decimal places.
     * 
     * <p>When false, a fixed number of decimal places is shown for all feature-values.
     * 
     * <p>e.g. when true, {@code 2} is shown instead of {@code 2.0000000000} or {@code code 6.71} instead of {@code 6.71000000000000}
     */
    @BeanField @Getter @Setter boolean visuallyShortenDecimals = true; 
    
    
    public FeatureExporterContext deriveContext(InputOutputContext context) {
        return new FeatureExporterContext(context, removeNaNColumns, visuallyShortenDecimals);
    }
}
