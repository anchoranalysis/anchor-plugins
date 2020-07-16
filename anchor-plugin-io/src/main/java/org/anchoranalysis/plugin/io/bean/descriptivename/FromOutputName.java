/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFileIndependent;
import org.anchoranalysis.io.namestyle.IndexableOutputNameStyle;

public class FromOutputName extends DescriptiveNameFromFileIndependent {

    // START BEAN PROPERTIES
    @BeanField private IndexableOutputNameStyle outputNameStyle;
    // END BEAN PROPERTIES

    @Override
    protected String createDescriptiveName(File file, int index) {
        return outputNameStyle.getPhysicalName(index);
    }

    public IndexableOutputNameStyle getOutputNameStyle() {
        return outputNameStyle;
    }

    public void setOutputNameStyle(IndexableOutputNameStyle outputNameStyle) {
        this.outputNameStyle = outputNameStyle;
    }
}
